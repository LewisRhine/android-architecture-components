/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.persistence.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.Transformations
import com.example.android.persistence.Action
import com.example.android.persistence.ActionCreator
import com.example.android.persistence.Actions
import com.example.android.persistence.db.DatabaseCreator
import com.example.android.persistence.statetree.ProductListStateTree
import java.util.*

class ProductListViewModel(application: Application) : AndroidViewModel(application) {
    private var currentState: ProductListStateTree = ProductListStateTree.Init()
    private val stateHistory: MutableMap<Date, ProductListStateTree> = mutableMapOf(Date() to currentState)

    val databaseCreator: DatabaseCreator =
            DatabaseCreator.getInstance(this.getApplication<Application>()).apply {
                createDb(getApplication())
            }

    val state: LiveData<ProductListStateTree> = Transformations.switchMap(ActionCreator) {
        MediatorLiveData<ProductListStateTree>().apply {
            addSource(productListMiddleware(it, databaseCreator)) {
                it?.let {
                    val newState = productListViewModelReducer(it, currentState)
                    if (newState != currentState) {
                        currentState = newState
                        stateHistory.put(Date(), newState)
                        value = newState
                    }
                }
            }
        }
    }
}

fun productListMiddleware(action: Action, databaseCreator: DatabaseCreator): LiveData<Action> {
    return when (action) {
        is Actions.ProductList.GetProductList -> {
            Transformations.switchMap(databaseCreator.isDatabaseCreated) { created ->
                if (created) {
                    MediatorLiveData<Action>().apply {
                        addSource(databaseCreator.database?.productDao()?.loadAllProducts()) {
                            it?.let { value = Actions.ProductList.GotProductList(it) }
                        }
                    }
                } else {
                    ActionCreator
                }
            }
        }
        else -> ActionCreator
    }
}

fun productListViewModelReducer(action: Action, currentState: ProductListStateTree): ProductListStateTree {
    return when (action) {
        is Actions.ProductList.GetProductList -> {
            when (currentState) {
                is ProductListStateTree.Loading -> currentState
                else -> ProductListStateTree.Loading()
            }
        }

        is Actions.ProductList.GotProductList -> {
            if (action.products.isEmpty()) ProductListStateTree.Done.EmptyList()
            else ProductListStateTree.Done.Ready(action.products)
        }
        else -> currentState
    }
}
