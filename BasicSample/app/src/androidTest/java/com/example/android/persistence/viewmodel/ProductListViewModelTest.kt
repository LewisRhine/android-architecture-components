package com.example.android.persistence.viewmodel

import com.example.android.persistence.Actions
import com.example.android.persistence.statetree.ProductListStateTree
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductListViewModelTest {

    @Test
    fun given_a_GetProductList_action() {
        val action = Actions.ProductList.GetProductList()
        // on EmptyList state
        val currentState = ProductListStateTree.Done.EmptyList()

        //it should return Loading state
        val newState = productListViewModelReducer(action, currentState)
        assertTrue(newState is ProductListStateTree.Loading)
    }
}