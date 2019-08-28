import os, sys, json
import requests
import vthread
import random
import psycopg2
import time
import queue
import math

userAgent = r'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 ' \
            r'Safari/537.36 '

host = 'http://127.0.0.1:8080'
reset_token = '123456'

concurrency = 150
order_amount = 150
product_amount = 1
user_amount = 150

user_list = []
user_dict = {}
product_list = []
gradient = 10
pipe = queue.Queue()
userQueue = queue.Queue()
request_results = []

conn = psycopg2.connect(database="seckill", user="seckill", password="seckill", host="container.ll-ap.cn", port="5001")

print("Database connected successfully")


def get_user_list(count, random_sort=True):
    assert count > 0
    print('Retrieving user list')
    cur = conn.cursor()
    sql = 'SELECT * FROM sessions'
    if random_sort:
        sql += ' ORDER BY random()'
    sql += ' LIMIT %d;' % count
    cur.execute(sql)
    for row in cur.fetchall():
        user = {
            'uid': row[0],
            'sessionid': row[1]
        }
        user_list.append(user)
        user_dict[row[0]] = user


def get_product_list(count, random_sort=True):
    assert count > 0
    print('Retrieving product list')
    cur = conn.cursor()
    sql = 'SELECT pid, count, price FROM products'
    if random_sort:
        sql += ' ORDER BY random()'
    sql += ' LIMIT %d;' % count
    cur.execute(sql)
    for row in cur.fetchall():
        product_list.append({
            'pid': row[0],
            'count': row[1],
            'price': row[2]
        })


def get_random_ip():
    return '%d.%d.%d.%d' % (random.randint(1, 254),random.randint(1, 254),random.randint(1, 254),random.randint(1, 254))


def make_request(method, user, url, data, passed=None):
    start = time.time()
    headers = {
        'User-Agent': userAgent,
        'X-Forwarded-For': get_random_ip(),
        'sessionid': user['sessionid'],
        'Content-Type': 'Application/json'
    }
    if method == 'post':
        ret = requests.post(host + url, headers=headers, data=json.dumps(data))
    else:
        ret = requests.get(host + url, headers=headers, params=data)
    end = time.time()
    resp = None
    if ret.status_code == 200:
        resp = json.loads(ret.content.decode())
    return {
        'url': url,
        'code': ret.status_code,
        'runtime': (end - start) * 1000,
        'response': resp,
        'passed': passed(resp) if passed is not None else True
    }


def get_product(pid, user):
    return make_request('get', user, '/product', {
        'pid': pid
    }, lambda x: pid == x['pid'])
    # print('Run time: %f' % runtime)
    # if res.status_code == 200:
    # print(res.content.decode())


def place_order(pid, user):
    ret = make_request('post', user, '/order', {
        'pid': pid,
        'uid': user['uid']
    })
    # print(ret)
    return ret


def pay_order(order_id, user, price):
    return make_request('post', user, '/pay', {
        'order_id': order_id,
        'uid': user['uid'],
        'price': price
    })


def get_result(user):
    return make_request('get', user, '/result', {
        'uid': user['uid']
    })


def run_reset(user):
    return make_request('post', user, '/reset', {
        'token': reset_token
    })


@vthread.pool(concurrency)
def run_all(user_index=None):
    user = None
    if user_index is not None:
        user = user_list[user_index]
    else:
        user = user_list[random.randint(0, len(user_list) - 1)]
    userQueue.put(user['uid'])
    product = product_list[random.randint(0, len(product_list) - 1)]
    ret_result = []
    print('Using uid %d & pid %d' % (user['uid'], product['pid']))
    ret_result.append(get_product(product['pid'], user))
    order_result = place_order(product['pid'], user)
    ret_result.append(order_result)
    created_order = order_result['response']
    if created_order is not None and created_order['code'] == 0:
        time.sleep(random.random())
        ret_result.append(pay_order(created_order['order_id'], user, product['price']))

    pipe.put(ret_result)


def prepare(random_sort=False):
    get_product_list(product_amount, random_sort)
    get_user_list(user_amount, random_sort)
    conn.close()


def stat_time(result):
    results = sorted(result, key=lambda x: int(x['runtime']))
    percent = 0
    max_time = None
    min_time = None
    response_code_stat = {}
    failed_result = []
    print('%\t\tTime')
    for i in range(len(results)):
        r = results[i]
        if max_time is None or r['runtime'] > max_time:
            max_time = r['runtime']
        if min_time is None or r['runtime'] < min_time:
            min_time = r['runtime']
        if response_code_stat.get(r['code']) is not None:
            response_code_stat[r['code']] = response_code_stat[r['code']] + 1
        else:
            response_code_stat[r['code']] = 1
        p = round(i / len(results) * 10000) / 100

        if math.floor(p) > percent + gradient:
            print("%.2f%%\t%d ms" % (p, r['runtime']))
            percent = math.floor(p)

        if r['passed'] is not None and not r['passed']:
            failed_result.append(r)
    print("100.0%%\t%d ms" % results[-1]['runtime'])
    print("Max: %d ms; Min: %d ms" % (max_time, min_time))
    print("Responses")
    print(response_code_stat)
    print("Failed requests")
    print(failed_result)
    print('')


if __name__ == '__main__':
    prepare()
    run_reset(user_list[0])
    request_results = []
    product_sold_count = {}
    userSet = set()
    for i in range(order_amount):
        run_all(i)
    for i in range(order_amount):
        item = pipe.get()
        for res in item:
            request_results.append(res)
    print('All requests finished. Retrieving result...')
    while not userQueue.empty():
        u = userQueue.get()
        userSet.add(u)

    for user in userSet:
        result_res = get_result(user_dict[user])['response']['data']
        for r in result_res:
            if product_sold_count.get(r['pid']) is not None:
                product_sold_count[r['pid']] += 1
            else:
                product_sold_count[r['pid']] = 1
    run_reset(user_list[0])
    stat_time(request_results)
    print('ORDER STATS')
    stat_time(list(filter(lambda x: x['url'] == '/order', request_results)))
    print('PRODUCT STATS')
    stat_time(list(filter(lambda x: x['url'] == '/product', request_results)))
    print('PAY STATS')
    stat_time(list(filter(lambda x: x['url'] == '/pay', request_results)))

    all_count = 0
    for product in product_list:
        sold = product_sold_count.get(product['pid'])
        if sold is not None and sold != 0:
            print("Product %d sold %d / %d" % (product['pid'], sold, product['count']))
            all_count += sold
    print('%d products sold' % all_count)
