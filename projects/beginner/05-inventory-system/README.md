# Inventory System

A console-based inventory management system demonstrating collections, CRUD operations, sorting, and file persistence.

## Features

- **Product management**: add, update, delete, and search products
- **Product fields**: ID, name, category, price, quantity
- **Low-stock alerts**: configure a threshold and list products below it
- **Sorting**: sort products by name, price, or quantity
- **File persistence**: inventory saved to `inventory.txt` (pipe-delimited) automatically
- **Inventory report**: summary with total value, category breakdown, and low-stock count

## How to run

```bash
javac Product.java InventorySystem.java
java InventorySystem
```

## Menu options

| Option | Description                          |
|--------|--------------------------------------|
| 1      | Add a new product                    |
| 2      | Update an existing product           |
| 3      | Delete a product                     |
| 4      | Search by name or category           |
| 5      | List all products                    |
| 6      | Set low-stock threshold              |
| 7      | Show low-stock alerts                |
| 8      | Sort products (name/price/quantity)  |
| 9      | Generate inventory report            |
| 0      | Exit (saves automatically)           |
