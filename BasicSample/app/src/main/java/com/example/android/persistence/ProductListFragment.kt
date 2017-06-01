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

package com.example.android.persistence

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleFragment
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.persistence.databinding.ListFragmentBinding
import com.example.android.persistence.statetree.ProductListStateTree
import com.example.android.persistence.ui.ProductAdapter
import com.example.android.persistence.viewmodel.ProductListViewModel

class ProductListFragment : LifecycleFragment() {

    private var mProductAdapter: ProductAdapter = ProductAdapter {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            (activity as MainActivity).show(it)
        }
    }

    lateinit private var mBinding: ListFragmentBinding

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate<ListFragmentBinding>(inflater!!, R.layout.list_fragment, container, false)

        mBinding.productsList.adapter = mProductAdapter

        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val viewModel = ViewModelProviders.of(this).get(ProductListViewModel::class.java)

        subscribeUi(viewModel)

        ActionCreator.dispatch(Actions.ProductList.GetProductList())
    }

    private fun subscribeUi(viewModel: ProductListViewModel) {
        viewModel.state.observe(this, Observer<ProductListStateTree> { productListStateTree ->
            when (productListStateTree) {
                is ProductListStateTree.Loading -> mBinding.isLoading = true

                is ProductListStateTree.Done.Ready -> {
                    mBinding.isLoading = false
                    mProductAdapter.setProductList(productListStateTree.products)
                }
            }
        })
    }

    companion object {
        val TAG = "ProductListViewModel"
    }
}
