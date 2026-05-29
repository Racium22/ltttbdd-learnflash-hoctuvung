package com.example.learnflash.giaoDien.manHinhChinh

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnflash.duLieu.khoDuLieu.KhoDuLieuDanhMuc
import com.example.learnflash.duLieu.khoDuLieu.KhoDuLieuTuVung
import com.example.learnflash.duLieu.local.thucThe.DanhMuc
import com.example.learnflash.duLieu.local.thucThe.TuVung
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Lớp ViewModel xử lý luồng dữ liệu một chiều cho Màn Hình Chính
class ManHinhChinhViewModel(
    private val khoDuLieu: KhoDuLieuTuVung,
    private val khoDuLieuDanhMuc: KhoDuLieuDanhMuc
) : ViewModel() {

    // Trạng thái StateFlow lưu ID danh mục đang được lọc — null = hiển thị tất cả
    private val _danhMucDangChon = MutableStateFlow<String?>(null)
    val danhMucDangChon: StateFlow<String?> = _danhMucDangChon

    // Luồng StateFlow toàn bộ từ vựng kết hợp với bộ lọc danh mục hiện tại
    val danhSachTuVung: StateFlow<List<TuVung>> = combine(
        khoDuLieu.layToanBoTuVung(),
        _danhMucDangChon
    ) { danhSach, danhMucId ->
        // Lọc danh sách theo danhMucId nếu có chọn — null thì trả toàn bộ
        if (danhMucId == null) danhSach
        else danhSach.filter { it.danhMucId == danhMucId }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Luồng StateFlow danh sách danh mục để hiển thị bộ lọc FilterChip
    val danhSachDanhMuc: StateFlow<List<DanhMuc>> = khoDuLieuDanhMuc.layToanBoDanhMuc()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Luồng dữ liệu StateFlow tổng hợp hai nguồn thống kê (Tổng số từ và Số từ đã thuộc)
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

    // Trạng thái (State) quản lý việc đóng/mở hộp thoại xác nhận xóa từ vựng
    val tuVungCanXoa = MutableStateFlow<TuVung?>(null)

    // Hàm thay đổi danh mục đang lọc — nhận null để hiển thị tất cả
    fun chonDanhMuc(danhMucId: String?) {
        _danhMucDangChon.value = danhMucId
    }

    // Thực thi thao tác xóa từ vựng trong Coroutine sau khi người dùng đồng ý
    fun xacNhanXoaTuVung() {
        tuVungCanXoa.value?.let { tuVung ->
            viewModelScope.launch {
                khoDuLieu.xoaTuVung(tuVung)
                tuVungCanXoa.value = null
            }
        }
    }

    // Sự kiện người dùng nhấn nút xóa, mở hộp thoại yêu cầu xác nhận
    fun yeuCauXoaTuVung(tuVung: TuVung) {
        tuVungCanXoa.value = tuVung
    }
}