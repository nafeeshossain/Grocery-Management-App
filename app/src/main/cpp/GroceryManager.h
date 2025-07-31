#ifndef GROCERYMANAGER_H
#define GROCERYMANAGER_H

#include <vector>
#include <string>

struct GroceryItem {
    std::string name;
    int quantity;
    float price;
};

class GroceryManager {
private:
    std::vector<GroceryItem> items;

public:
    void addItem(const std::string& name, int quantity, float price);
    std::string getItems() const;
    float getTotalCost() const;
    void clearItems(); // ✅ Make sure this is here
};

#endif // GROCERYMANAGER_H
