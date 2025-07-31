#include <jni.h>
#include <string>
#include "GroceryManager.h"

static GroceryManager groceryManager;

extern "C" {

JNIEXPORT void JNICALL
Java_com_tutorials_grocerymanagerapp_MainActivity_addItemNative(
        JNIEnv* env,
        jobject,
        jstring name,
        jint quantity,
        jfloat price) {
    const char* nativeName = env->GetStringUTFChars(name, nullptr);
    groceryManager.addItem(nativeName, quantity, price);
    env->ReleaseStringUTFChars(name, nativeName);
}

JNIEXPORT jstring JNICALL
Java_com_tutorials_grocerymanagerapp_MainActivity_getItemsNative(
        JNIEnv* env,
        jobject) {
    std::string items = groceryManager.getItems();
    return env->NewStringUTF(items.c_str());
}

JNIEXPORT jfloat JNICALL
Java_com_tutorials_grocerymanagerapp_MainActivity_getTotalCostNative(
        JNIEnv* env,
        jobject) {
    return groceryManager.getTotalCost();
}

JNIEXPORT void JNICALL
Java_com_tutorials_grocerymanagerapp_MainActivity_clearItemsNative(
        JNIEnv* env,
        jobject) {
    groceryManager.clearItems();  // ✅ Now this works
}

}
