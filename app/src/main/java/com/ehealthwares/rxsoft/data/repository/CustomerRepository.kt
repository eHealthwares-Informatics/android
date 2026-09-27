package com.rxsoft.mobile.data.repository

import com.rxsoft.mobile.data.remote.api.CustomersApi
import com.rxsoft.mobile.data.remote.dto.CreateCustomerRequest
import com.rxsoft.mobile.data.remote.dto.CustomerDto
import javax.inject.Inject

class CustomerRepository @Inject constructor(
    private val customersApi: CustomersApi
) {
    suspend fun listCustomers(search: String? = null, page: Int = 1, limit: Int = 20): Result<List<CustomerDto>> {
        return try {
            val params = mutableMapOf("page" to page.toString(), "limit" to limit.toString())
            search?.let { params["search"] = it }
            Result.success(customersApi.listCustomers(params).data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCustomer(id: String): Result<CustomerDto> {
        return try {
            Result.success(customersApi.getCustomer(id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createCustomer(name: String, phone: String?, email: String?): Result<CustomerDto> {
        return try {
            val request = CreateCustomerRequest(name = name, phone = phone, email = email)
            Result.success(customersApi.createCustomer(request))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
