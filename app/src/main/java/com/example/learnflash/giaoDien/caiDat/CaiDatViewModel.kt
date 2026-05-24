package com.example.learnflash.giaoDien.caiDat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnflash.duLieu.local.dataStore.CaiDatDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Lớp ViewModel quản lý luồng StateFlow cài đặt ứng dụng từ DataStore
class CaiDatViewModel(private val caiDatDataStore: CaiDatDataStore) : ViewModel() {

    // Luồng StateFlow phát tán trạng thái bật/tắt giao diện tối xuống UI
    val giaoDienToi: StateFlow<Boolean> = caiDatDataStore.giaoDienToiFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // Luồng StateFlow phát tán giá trị mục tiêu học hàng ngày xuống UI
    val mucTieuHocNgay: StateFlow<Int> = caiDatDataStore.mucTieuHocNgayFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 10
        )

    // Thực thi tác vụ lưu trạng thái giao diện tối vào DataStore qua Coroutine
    fun doiGiaoDienToi(batTat: Boolean) {
        viewModelScope.launch {
            caiDatDataStore.luuTrangThaiGiaoDien(batTat)
        }
    }

    // Thực thi tác vụ lưu mục tiêu học hàng ngày vào DataStore qua Coroutine
    fun doiMucTieuHocNgay(mucTieu: Int) {
        viewModelScope.launch {
            caiDatDataStore.luuMucTieuHocNgay(mucTieu)
        }
    }
}