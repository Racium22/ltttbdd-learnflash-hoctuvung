package com.example.learnflash.giaoDien.manHinhChinh

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnflash.duLieu.khoDuLieu.KhoDuLieuDanhMuc
import com.example.learnflash.duLieu.khoDuLieu.KhoDuLieuTuVung
import com.example.learnflash.duLieu.local.thucThe.DanhMuc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Lớp ViewModel xử lý luồng dữ liệu một chiều cho Màn Hình Chính
class ManHinhChinhViewModel(
    private val khoDuLieu: KhoDuLieuTuVung,
    private val khoDuLieuDanhMuc: KhoDuLieuDanhMuc
) : ViewModel() {

    // Trạng thái StateFlow kiểm soát hiển thị màn hình loading khi khởi tạo dữ liệu Firebase
    private val _dangKhoiTao = MutableStateFlow(true)
    val dangKhoiTao: StateFlow<Boolean> = _dangKhoiTao.asStateFlow()

    // Trạng thái StateFlow lưu thông báo lỗi nếu tải Firebase thất bại
    private val _loiKhoiTao = MutableStateFlow("")
    val loiKhoiTao: StateFlow<String> = _loiKhoiTao.asStateFlow()

    // Luồng StateFlow danh sách danh mục để hiển thị lưới trang chủ
    val danhSachDanhMuc: StateFlow<List<DanhMuc>> = khoDuLieuDanhMuc.layToanBoDanhMuc()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Luồng StateFlow tổng hợp thống kê tổng số từ và số từ đã thuộc
    val thongKeTuVung: StateFlow<Pair<Int, Int>> = combine(
        khoDuLieu.demTongSoTuVung(),
        khoDuLieu.demSoTuDaThuoc()
    ) { tongSo, daThuoc ->
        Pair(tongSo, daThuoc)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Pair(0, 0)
    )

    init {
        // Kích hoạt tác vụ kiểm tra và tải dữ liệu Firebase ngay khi ViewModel được tạo
        khoiTaoDuLieu()
    }

    // Thực thi tác vụ bất đồng bộ kiểm tra Room và tải Firestore nếu cần
    private fun khoiTaoDuLieu() {
        viewModelScope.launch {
            _dangKhoiTao.value = true
            val thanhCong = khoDuLieu.khoiTaoDuLieuMacDinh()
            if (!thanhCong) {
                // Vẫn cho phép dùng app với dữ liệu Room cũ dù Firebase thất bại
                _loiKhoiTao.value = "Không thể kết nối Firebase — dùng dữ liệu ngoại tuyến"
            }
            _dangKhoiTao.value = false
        }
    }

    // Cho phép người dùng thử tải lại nếu trước đó gặp lỗi
    fun taiLai() {
        _loiKhoiTao.value = ""
        khoiTaoDuLieu()
    }
}