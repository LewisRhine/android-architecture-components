package com.example.android.persistence.statetree

import com.example.android.persistence.model.Product

sealed class ProductListStateTree {
    class Loading : ProductListStateTree()

    sealed class Done : ProductListStateTree() {
        class EmptyList : Done()
        data class Ready(val products: List<Product>) : Done()
        data class Error(val message: String) : Done()
    }
}