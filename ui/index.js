const socket = new SockJS('http://localhost:8080/app');
const client = Stomp.over(socket);
const ctx = document.getElementById('chart').getContext('2d');
ctx.canvas.width = 1000;
ctx.canvas.height = 250;

const barData = [];
const chart = new Chart(ctx, {
    type: 'candlestick',
    data: {
        datasets: [{
            label: 'Candlestick Data ( EST )',
            data: barData
        }]
    },
    responsive: true
});

client.connect({}, () => {
    client.subscribe('/topic/stock-tick', (res) => pushElementToChartData(res));
});

document.querySelector("body").addEventListener("keyup", event => {
    if(event.key !== "Enter") return;
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

    if (barData.length === 100) {
        barData.shift();
    }

    barData.push({
        x: new Date(...message.timestamp).valueOf(),
        o: message.open,
        h: message.high,
        l: message.low,
        c: message.close,
        v: message.volume
    });

    document.getElementById("candle").innerText = message.candlePattern;

    chart.config.data.datasets[0].data = barData;
    chart.update();
};
