const redis = require('redis');
const {Pool} = require('pg');

const redisHost = 'container.ll-ap.cn';
const redisPort = 6379;
const pgHost = 'container.ll-ap.cn';
const pgPort = 5432;
const pgUser = 'postgres';
const pgPass = '';
const pgDatabase = 'seckill';

const pool = new Pool({
    user: pgUser,
    host: pgHost,
    database: pgDatabase,
    password: pgPass,
    port: pgPort
});
console.log("Database consumed successfully");

pool.on('error', (err, client) => {
    console.error("DB error")
    console.error({err});
    process.exit(-1);
});


const queueName = "orderQueue";
const payHashName = "paidOrder";

client = redis.createClient(redisPort, redisHost);
console.log("Redis connected successfully");

client.on("error", err => {
    console.error(err);
});

function consume() {
    return new Promise((resolve, reject) => {
        client.brpop(queueName, 10, (err, res) => {
            if (err) {
                reject(err);
            }
            else {
                if (res) console.log("Message received");
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
                if (res) console.log("Found token " + res);
                resolve(res);
            }
        })
    })
}

function requestPayToken(orderId, uid, price) {
    // TODO: get pay token using axios
}

/*
public class PayMessage implements Serializable {
    private String order_id;
    private String token;
}
public class OrderMessage implements Serializable {
    private int uid;
    private int pid;
    private int price;
    private String order_id;
}
 */
async function startConsume() {
    console.log("start consuming");
    while (client.connected) {
        const res = await consume().catch(err => {
            console.error({err});
        });
        if (res && res.length > 1) {
            // TODO: proceed message
            let message = JSON.parse(res[1]);
            const payToken = await getPayToken(message.order_id);
            console.log(message);
        }
    }
}

async function end() {
    console.log("Bye");
    client.end(true);
    await pool.end().catch(err => {
        console.error(err);
    });
}

process.on("SIGTERM", end);
process.on("SIGKILL", end);
process.on("SIGINT", end);

startConsume();

