const redis = require('redis');
const {Pool} = require('pg');
const axios = require('axios');

const redisHost = 'container.ll-ap.cn';
const redisPort = 6379;
const pgHost = 'container.ll-ap.cn';
const pgPort = 5432;
const pgUser = 'postgres';
const pgPass = '';
const pgDatabase = 'seckill';

// create an axios instance
const request = axios.create({
    baseURL: 'http://127.0.0.1:8889', // api 的 base_url
    timeout: 1500 // request timeout
});

// request interceptor
request.interceptors.request.use(
    config => {
        return config
    },
    error => {
        // Do something with request error
        console.log(error); // for debug
        Promise.reject(error)
    }
);

// response interceptor
request.interceptors.response.use(
    // response => response,
    response => {
        // console.log(response);
        return response
    },
    error => {
        console.error({ error });
        return Promise.reject(error)
    }
);

const pool = new Pool({
    user: pgUser,
    host: pgHost,
    database: pgDatabase,
    password: pgPass,
    port: pgPort,
    max: 5
});
console.log("Database consumed successfully");

pool.on('error', (err, client) => {
    console.error("DB error");
    console.error({err});
    process.exit(-1);
});

const queueName = "orderQueue";
const payHashName = "paidOrder";
const createOrderSql = "INSERT INTO orders (order_id, uid, pid, price, status, token) values ($1, $2, $3, $4, $5, $6)";
client = redis.createClient(redisPort, redisHost);

client.on("error", err => {
    console.error(err);
});

client.on('connect', res => {
    console.log('Redis connected successfully');
    startConsume();
});

function consume() {
    return new Promise((resolve, reject) => {
        client.brpop(queueName, 10, (err, res) => {
            if (err) {
                reject(err);
            }
            else {
                resolve(res);
            }
        });
    })
}

function getPayToken(orderId) {
    return new Promise((resolve, reject) => {
        client.hget(payHashName, orderId, (err, res) => {
            if (err) {
                reject(err);
            }
            else {
                resolve(res);
            }
        })
    })
}

function deleteCachedToken(orderId) {
    return new Promise((resolve, reject) => {
        client.hdel(payHashName, orderId, (err, res) => {
            if (err) reject(err);
            else resolve(res);
        })
    })
}

async function requestPayToken(orderId, uid, price) {
    const res = await request({
        url: 'http://127.0.0.1:8889/token',
        method: 'post',
        data: {
            order_id: orderId,
            uid: uid,
            price: price
        }
    });
    if (res && res.data && res.data.token) {
        return res.data.token;
    }
    return null;
}
/*public class PayMessage implements Serializable {
    private String order_id;
    private String token;
}
public class OrderMessage implements Serializable {
    private int uid;
    private int pid;
    private int price;
    private String order_id;
}*/
function createOrder(order) {
    /*
    private int uid;
    private int pid;
    private int price;
    private int status = 0;
    private String token;
    (order_id, uid, pid, price, status, token)
     */
    const orderArgs = [
        order.order_id,
        order.uid,
        order.pid,
        order.price,
        order.status ? order.status : 0,
        order.token ? order.token : ''
    ];
    console.log('Creating order...');
    pool.query(createOrderSql, orderArgs, (err, res) => {
        if (err) {
            console.error("Cannot create order");
            console.error({err});
        }
        else {
            console.log(res.rowCount + " order created.");
            // console.log(res);
        }
    });
}

async function startConsume() {
    console.log("Start consuming");
    if (!client.connected) {
        console.error("Redis server not connected");
    }
    while (client.connected) {
        const res = await consume().catch(err => {
            console.error({err});
        });
        // console.log(res);
        if (res && res.length > 1) {
            console.log("Received 1 message");
            let message = JSON.parse(res[1]);
            console.log(message);
            let cachedToken = false;
            let payToken = await getPayToken(message.order_id);
            if (payToken) {
                console.log('Found cached token: ' + payToken);
                cachedToken = true;
                message.status = 1;
            }
            else {
                message.status = 0;
                console.log("Requesting a new token...");
                payToken = await requestPayToken(message.order_id, message.uid, message.price)
                    .catch(err => {
                    console.error(err);
                });
                console.log('Requested token: ' + payToken);
            }
            createOrder({
                order_id: message.order_id,
                uid: message.uid,
                pid: message.pid,
                price: message.price,
                status: message.status,
                token: payToken,
            });
            // invalidate token
            if (cachedToken) deleteCachedToken(message.order_id).then(res => {
                console.log('Cached token invalidated');
            }).catch(err => {
                console.error('Cannot invalidate cached token');
            })
        }
    }
}

async function end() {
    console.log("Bye");
    client.quit();
    await pool.end().catch(err => {
        console.error(err);
    });
}

process.on("SIGTERM", end);
process.on("SIGKILL", end);
process.on("SIGINT", end);


// requestPayToken('111', 2, 3);
