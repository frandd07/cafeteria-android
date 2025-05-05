package com.example.cafeteria_android.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Singleton que mantiene los ítems del carrito en memoria */
public class CartRepository {
    private static final CartRepository INSTANCE = new CartRepository();
    private final List<CartItem> items = new ArrayList<>();

    private CartRepository() {}

    public static CartRepository getInstance() {
        return INSTANCE;
    }

    /** Añade un nuevo ítem al carrito */
    public void addItem(CartItem item) {
        // si ya existía el mismo producto+extras, podrías combinar cantidades aquí
        items.add(item);
    }

    /** Obtiene copia de los ítems actuales */
    public List<CartItem> getItems() {
        return new ArrayList<>(items);
    }

    /** Elimina todos los ítems del carrito */
    public void clear() {
        items.clear();
    }

    /** Elimina un ítem específico del carrito */
    public void removeItem(CartItem item) {
        Iterator<CartItem> it = items.iterator();
        while (it.hasNext()) {
            CartItem ci = it.next();
            if (ci.equals(item)) {
                it.remove();
                break;
            }
        }
    }
}
