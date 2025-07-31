#include "GroceryManager.h"
#include <sstream>

void GroceryManager::addItem(const std::string& name, int quantity, float price) {
    items.push_back({name, quantity, price});
}

std::string GroceryManager::getItems() const {
    std::ostringstream oss;
    for (const auto& item : items) {
        oss << item.name << " - Qty: " << item.quantity << " - ₹" << item.price << "\n";
    }
    return oss.str();
}

float GroceryManager::getTotalCost() const {
    float total = 0.0f;
    for (const auto& item : items) {
        total += item.quantity * item.price;
    }
    return total;
}
void GroceryManager::clearItems() {
    items.clear();
}

