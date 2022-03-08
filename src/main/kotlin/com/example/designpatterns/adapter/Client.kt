package com.example.designpatterns.adapter

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule

// "com.fasterxml.jackson.core:jackson-databind:2.13.1"
// "com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.13.1"
// "com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1"

// datetime : RFC 3339 format (2022-02-23T11:00:00Z)
data class Stock(val name: String, val datetime: String, val price: Double) {
    constructor(json: JsonNode) : this(
        json["name"].toString(), json["datetime"].toString(), json["price"].numberValue().toDouble()
    )

    override fun toString(): String {
        return "Stock(name='$name', datetime='$datetime', price=$price)"
    }
}

interface ClientInterface {
    fun sendStockData(stockXmlString: String): Stock
}

interface StockService {
    fun send(json: JsonNode): Stock
}

class StockJsonService : StockService {
    override fun send(json: JsonNode): Stock {
        // doing some complex things here maybe?
        return Stock(json)
    }
}

class XmlToJsonAdapter(
    private val stockService: StockService = StockJsonService()
) : ClientInterface {
    // 성능, 메모리 효율성 등 이슈는 잠시 마음안에 넣어주세요.
    override fun sendStockData(stockXmlString: String): Stock {
        val kotlinModule = KotlinModule.Builder().build()
        val jsonMapper = ObjectMapper().registerModule(kotlinModule)
        val xmlMapper = XmlMapper().registerModule(kotlinModule)
        val stock = xmlMapper.readValue(stockXmlString, Stock::class.java)
        return stockService.send(
            jsonMapper.readTree(jsonMapper.writeValueAsBytes(stock))
        )
    }
}

class Client(
    private val stockDataSendingAdapter: ClientInterface = XmlToJsonAdapter()
) {
    fun sendStockData(xml: String): Stock {
        return stockDataSendingAdapter.sendStockData(xml)
    }
}

fun main() {
    val client = Client()
    val xml = """
        <Stock>
            <name>Apple</name>
            <datetime>2022-02-23T11:00:00Z</datetime>
            <price>115.0</price>
        </Stock>
    """.trimIndent()
    val stockSent = client.sendStockData(xml)
    print(stockSent)

    //...
}
