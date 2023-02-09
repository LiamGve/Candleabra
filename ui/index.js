const socket = new SockJS('http://localhost:8080/app');
const client = Stomp.over(socket);
const ctx = document.getElementById('chart').getContext('2d');
ctx.canvas.width = 1000;
ctx.canvas.height = 250;

const barData = [];
const chart = new Chart(ctx, {
    type: 'candlestick',
    data: {
        datasets: [
            {
                label: 'Candlestick Data ( EST )',
                data: barData
            }
        ]
    },
    responsive: true
});

client.connect({}, () => {
    client.subscribe('/topic/stock-tick', (res) => pushElementToChartData(res));
});

document.querySelector("body").addEventListener("keyup", event => {
    if (event.key !== "Enter") return;
    document.querySelector("#submit").click();
    event.preventDefault();
});

const connect = () => {
    const stock = document.getElementById("stock").value;
    const investmentAmount = document.getElementById("investmentAmount").value;
    client.send('/start', {}, `{"shortCode": "${stock}", "investmentAmount": ${investmentAmount}}`);
}

const pushElementToChartData = (res) => {
    const message = JSON.parse(res.body);

    if (barData.length === 50) {
        barData.shift();
    }
    // month is 0 indexed
    message.timestamp[1] = message.timestamp[1] - 1;

    const date = new Date(...message.timestamp);

    barData.push({
        x: date.valueOf(),
        o: message.open,
        h: message.high,
        l: message.low,
        c: message.close,
        v: message.volume,
        candle: message.candlePattern
    });

    chart.config.data.datasets[0].data = barData;
    chart.config.options = {
        annotation: {
            drawTime: 'afterDatasetsDraw',
            annotations: [
                {
                    type: 'line',
                    id: 'vline' + date.valueOf(),
                    mode: 'vertical',
                    scaleID: 'x-axis-0',
                    value: date.valueOf(),
                    borderColor: 'green',
                    borderWidth: 1,
                    label: {
                        enabled: true,
                        position: "center",
                        content: 50
                    }
                }
            ]
        }
    };
    chart.update();

    document.getElementById("liquid").innerText = "Liquid Cash $" + message.liquidCash;

    if (message.action !== 'NOTHING') {
        createLogItem(message, date);
    }
};

const createLogItem = (stock, date) => {
    const list = document.getElementById("log-list");

    const li = document.createElement("li");

    li.id = stock.action;
    li.innerHTML = `
        <label>Type</label><p>${stock.action}</p>
        <label>Time</label><p>${date.toISOString().replace(/T/, " ").replace(/:00.000Z/, "")}</p>
    `;

    list.appendChild(li);
}