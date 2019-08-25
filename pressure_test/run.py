import os, sys, json
import requests
import vthread
import random
import psycopg2
import time
import queue
import itertools

userAgent = r'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 ' \
            r'Safari/537.36 '

host = 'http://127.0.0.1:8080'
reset_token = '123456'

concurrency = 10
order_amount = 10
product_amount = 100
user_amount = 100

user_list = []
product_list = []

pipe = queue.Queue()
request_results = []

conn = psycopg2.connect(database="seckill", user="postgres", password="", host="container.ll-ap.cn", port="5432")

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
        user_list.append({
            'uid': row[0],
            'sessionid': row[1]
        })


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


def make_request(method, user, url, data):
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
    return ret, (end - start) * 1000


def get_product(pid, user):
    res, runtime = make_request('get', user, '/product', {
        'pid': pid
    })
    print('Run time: %f' % runtime)
    # if res.status_code == 200:
    print(res.content.decode())
    return {
        'url': '/product',
        'code': res.status_code,
        'runtime': runtime,
        'passed': pid == json.loads(res.content.decode())['pid'] if res.status_code == 200 else False
    }


def place_order(pid, user):
    res, runtime = make_request('post', user, '/order', {
        'pid': pid,
        'uid': user['uid']
    })


def pay_order(order_id, user, price):
    res, runtime = make_request('post', user, '/pay', {
        'order_id': order_id,
        'uid': user['uid'],
        'price': price
    })


def get_result(user):
    res, runtime = make_request('get', user, '/result', {
        'uid': user['uid']
    })


def run_reset():
    pass


@vthread.pool(concurrency)
def run_all():
    user = user_list[random.randint(0, len(user_list) - 1)]
    product = product_list[random.randint(0, len(product_list) - 1)]
    ret_result = []
    print('Using uid %d & pid %d' % (user['uid'], product['pid']))
    ret_result.append(get_product(product['pid'], user))

    pipe.put(ret_result)


def prepare(random_sort=False):
    get_product_list(product_amount, random_sort)
    get_user_list(user_amount, random_sort)
    conn.close()


if __name__ == '__main__':
    prepare()
    for i in range(order_amount):
        run_all()
    request_results = list(itertools.chain(*[pipe.get() for i in range(order_amount)]))
    print(json.dumps(request_results, indent=2))

