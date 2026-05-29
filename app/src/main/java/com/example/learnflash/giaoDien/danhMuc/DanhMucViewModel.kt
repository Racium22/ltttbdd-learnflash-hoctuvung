package com.example.learnflash.giaoDien.danhMuc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnflash.duLieu.khoDuLieu.KhoDuLieuDanhMuc
import com.example.learnflash.duLieu.local.thucThe.DanhMuc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

// Lớp ViewModel quản lý luồng State và nghiệp vụ cho màn hình Danh mục
class DanhMucViewModel(private val khoDuLieu: KhoDuLieuDanhMuc) : ViewModel() {

    // Luồng StateFlow phát tán danh sách danh mục từ Room — tự động cập nhật khi có thay đổi
    val danhSachDanhMuc: StateFlow<List<DanhMuc>> = khoDuLieu.layToanBoDanhMuc()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Trạng thái điều khiển hiển thị hộp thoại thêm danh mục mới
    private val _hienHopThoaiThem = MutableStateFlow(false)
    val hienHopThoaiThem: StateFlow<Boolean> = _hienHopThoaiThem.asStateFlow()

    // Trạng thái lưu đối tượng DanhMuc đang chờ xác nhận xóa
    private val _danhMucCanXoa = MutableStateFlow<DanhMuc?>(null)
    val danhMucCanXoa: StateFlow<DanhMuc?> = _danhMucCanXoa.asStateFlow()

    // Trạng thái lưu thông báo kết quả thao tác để hiển thị SnackBar
    private val _thongBao = MutableStateFlow("")
    val thongBao: StateFlow<String> = _thongBao.asStateFlow()

    // Trạng thái quản lý chuỗi nhập liệu tên danh mục mới trong hộp thoại
    private val _tenDanhMucMoi = MutableStateFlow("")
    val tenDanhMucMoi: StateFlow<String> = _tenDanhMucMoi.asStateFlow()

    // Trạng thái quản lý chuỗi nhập liệu mô tả danh mục mới
    private val _moTaDanhMucMoi = MutableStateFlow("")
    val moTaDanhMucMoi: StateFlow<String> = _moTaDanhMucMoi.asStateFlow()

    // Hàm cập nhật State khi người dùng nhập tên danh mục
    fun capNhatTenDanhMuc(ten: String) {
        _tenDanhMucMoi.value = ten
    }

    // Hàm cập nhật State khi người dùng nhập mô tả danh mục
    fun capNhatMoTaDanhMuc(moTa: String) {
        _moTaDanhMucMoi.value = moTa
    }

    // Mở hộp thoại nhập liệu thêm danh mục mới
    fun moHopThoaiThem() {
        _tenDanhMucMoi.value = ""
        _moTaDanhMucMoi.value = ""
        _hienHopThoaiThem.value = true
    }

    // Đóng hộp thoại nhập liệu và xóa trắng dữ liệu tạm
    fun dongHopThoaiThem() {
        _hienHopThoaiThem.value = false
    }

    // Thực thi xác thực và lưu danh mục mới vào Room Database qua Repository
    fun luuDanhMucMoi() {
        if (_tenDanhMucMoi.value.isBlank()) {
            _thongBao.value = "Tên danh mục không được để trống"
            return
        }
        viewModelScope.launch {
            val danhMucMoi = DanhMuc(
                // Tạo ID duy nhất bằng UUID để tránh xung đột khi đồng bộ Firestore
                id = UUID.randomUUID().toString(),
                ten = _tenDanhMucMoi.value.trim(),
                moTa = _moTaDanhMucMoi.value.trim(),
                laMacDinh = false
            )
            khoDuLieu.luuDanhMuc(danhMucMoi)
            _hienHopThoaiThem.value = false
            _thongBao.value = "Đã thêm danh mục \"${danhMucMoi.ten}\""
        }
    }

    // Sự kiện người dùng nhấn xóa — gán đối tượng cần xóa vào State để hiện hộp thoại xác nhận
    fun yeuCauXoaDanhMuc(danhMuc: DanhMuc) {
        _danhMucCanXoa.value = danhMuc
    }

    // Thực thi xóa danh mục sau khi người dùng xác nhận qua hộp thoại
    fun xacNhanXoaDanhMuc() {
        _danhMucCanXoa.value?.let { danhMuc ->
            viewModelScope.launch {
                // Kiểm tra số từ còn trong danh mục trước khi xóa
                val soTu = khoDuLieu.demSoTuThuocDanhMuc(danhMuc.id)
                if (soTu > 0) {
                    _thongBao.value = "Không thể xóa: danh mục còn $soTu từ vựng"
                } else {
                    val ketQua = khoDuLieu.xoaDanhMuc(danhMuc)
                    _thongBao.value = if (ketQua.isSuccess)
                        "Đã xóa danh mục \"${danhMuc.ten}\""
                    else
                        ketQua.exceptionOrNull()?.message ?: "Xóa thất bại"
                }
                _danhMucCanXoa.value = null
            }
        }
    }

    // Hủy thao tác xóa — đóng hộp thoại xác nhận
    fun huyXoaDanhMuc() {
        _danhMucCanXoa.value = null
    }

    // Đặt lại chuỗi thông báo về rỗng sau khi SnackBar đã hiển thị
    fun daHienThiThongBao() {
        _thongBao.value = ""
    }
}