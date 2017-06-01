package com.example.android.persistence

import android.arch.lifecycle.MutableLiveData
import com.example.android.persistence.db.entity.ProductEntity

object ActionCreator : MutableLiveData<Action>() {
    fun dispatch(action: Action) {
        value = action
    }
}

sealed class Actions : Action {
    sealed class ProductList : Actions() {
        class GetProductList : ProductList()
        data class GotProductList(val products: List<ProductEntity>) : ProductList()
    }
}

interface Action
