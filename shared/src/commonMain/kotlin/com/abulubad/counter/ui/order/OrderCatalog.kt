package com.abulubad.counter.ui.order

data class OrderItem(
    val name: String,
    val sku: String,
    val priceMinorUnits: Long,
    val stock: Int,
)

data class OrderCategory(
    val name: String,
    val items: List<OrderItem>,
)

fun orderCatalog(): List<OrderCategory> = listOf(
    OrderCategory(
        "Apparel",
        listOf(
            OrderItem("Course polo, navy", "AP-1042", 6800, 14),
            OrderItem("Course polo, sand", "AP-1043", 6800, 3),
            OrderItem("Quarter zip", "AP-2210", 11200, 6),
            OrderItem("Rain shell", "AP-3301", 18400, 2),
            OrderItem("Cap, structured", "AP-4102", 3200, 22),
            OrderItem("Cap, rope", "AP-4108", 3600, 0),
            OrderItem("Visor", "AP-4120", 2800, 11),
            OrderItem("Belt, leather", "AP-5010", 5400, 4),
        ),
    ),
    OrderCategory(
        "Balls",
        listOf(
            OrderItem("Sleeve of three", "BL-1001", 1650, 40),
            OrderItem("Dozen, tour", "BL-1012", 5900, 12),
        ),
    ),
    OrderCategory(
        "Gloves",
        listOf(
            OrderItem("Glove, cabretta", "GL-0031", 2900, 18),
            OrderItem("Glove, rain", "GL-0044", 3400, 7),
        ),
    ),
    OrderCategory(
        "Accessories",
        listOf(
            OrderItem("Towel, tri-fold", "AC-2001", 2400, 9),
            OrderItem("Divot tool", "AC-2210", 1900, 15),
            OrderItem("Tee pack", "AC-2301", 600, 60),
            OrderItem("Umbrella, 68in", "AC-3400", 7200, 1),
        ),
    ),
    OrderCategory(
        "Green fees",
        listOf(
            OrderItem("Weekday 18", "GF-18WD", 6500, 99),
            OrderItem("Twilight", "GF-TWI", 4500, 99),
            OrderItem("Member 9", "GF-9MBR", 3000, 99),
        ),
    ),
)