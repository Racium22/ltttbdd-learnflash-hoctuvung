package com.example.learnflash.giaoDien.onTapTheFlash

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnflash.duLieu.khoDuLieu.KhoDuLieuTuVung
import com.example.learnflash.duLieu.local.thucThe.LichSuOnTap
import com.example.learnflash.duLieu.local.thucThe.TuVung
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

// Lớp ViewModel xử lý nghiệp vụ lật thẻ và thuật toán lặp ngắt quãng (Spaced Repetition)
class OnTapViewModel(
    private val khoDuLieu: KhoDuLieuTuVung,
    // Tham số lọc theo danh mục — null nghĩa là ôn tất cả các danh mục
    private val danhMucId: String? = null
) : ViewModel() {

    // Trạng thái (State) lưu trữ danh sách từ vựng cần học trong buổi này
    private val _danhSachOnTap = mutableStateOf<List<TuVung>>(emptyList())

    // Trạng thái trỏ tới chỉ số thứ tự của từ vựng hiện tại đang hiển thị
    private val _chiSoHienTai = mutableStateOf(0)
    val chiSoHienTai: State<Int> = _chiSoHienTai

    // Trạng thái quản lý mặt hiển thị của thẻ (false: mặt trước, true: mặt sau)
    private val _dangLatThe = mutableStateOf(false)
    val dangLatThe: State<Boolean> = _dangLatThe

    // Trạng thái kiểm tra xem toàn bộ phiên học đã kết thúc hay chưa
    private val _hoanThanhHoc = mutableStateOf(false)
    val hoanThanhHoc: State<Boolean> = _hoanThanhHoc

    // Biến lưu trữ số liệu thống kê trong phiên học
    private var soTuDung = 0
    private var soTuSai = 0

    // Thực thi khởi tạo luồng dữ liệu — lấy từ vựng tới hạn theo danh mục hoặc toàn bộ
    fun batDauPhienHoc() {
        viewModelScope.launch {
            val thoiGianHienTai = System.currentTimeMillis()
            // Lọc theo danh mục nếu được truyền vào, ngược lại lấy toàn bộ
            val danhSach = if (danhMucId != null) {
                khoDuLieu.layTuVungCanOnTapTheoDanhMuc(thoiGianHienTai, danhMucId).firstOrNull()
            } else {
                khoDuLieu.layTuVungCanOnTap(thoiGianHienTai).firstOrNull()
            } ?: emptyList()

            _danhSachOnTap.value = danhSach
            _chiSoHienTai.value = 0
            _hoanThanhHoc.value = danhSach.isEmpty()
            soTuDung = 0
            soTuSai = 0
        }
    }

    // Hàm trả về đối tượng từ vựng tại vị trí chỉ số hiện tại
    fun layTuVungHienTai(): TuVung? = _danhSachOnTap.value.getOrNull(_chiSoHienTai.value)

    // Hàm xử lý sự kiện kích hoạt lật thẻ Flashcard
    fun latThe() {
        _dangLatThe.value = !_dangLatThe.value
    }

    // Thực thi logic cập nhật cấp độ ghi nhớ khi người dùng chọn Nhớ hoặc Quên
    fun danhGiaTuVung(nhoDung: Boolean) {
        val tuHienTai = layTuVungHienTai() ?: return
        viewModelScope.launch {
            val capDoMoi = if (nhoDung) tuHienTai.capDoSrs + 1 else 0
            val ngayTiepTheo = System.currentTimeMillis() + (capDoMoi * 24 * 60 * 60 * 1000L)
            val daThuocHoanToan = capDoMoi >= 5

            val tuCapNhat = tuHienTai.copy(
                capDoSrs = capDoMoi,
                ngayOnTapTiepTheo = ngayTiepTheo,
                daThuoc = daThuocHoanToan
            )
            // Ghi nhận thay đổi vào Room và đồng bộ tiến độ SRS lên Firestore
            khoDuLieu.capNhatTienDoSrs(tuCapNhat)

            if (nhoDung) soTuDung++ else soTuSai++
            chuyenSangTuTiepTheo()
        }
    }

    // Xử lý logic tăng chỉ số mảng hoặc ghi nhận lịch sử khi kết thúc phiên học
    private suspend fun chuyenSangTuTiepTheo() {
        if (_chiSoHienTai.value < _danhSachOnTap.value.size - 1) {
            _chiSoHienTai.value += 1
            // Tự động úp thẻ lại khi chuyển sang từ mới
            _dangLatThe.value = false
        } else {
            val lichSu = LichSuOnTap(
                soTuDaHoc = _danhSachOnTap.value.size,
                soTuDung = soTuDung,
                soTuSai = soTuSai
            )
            khoDuLieu.themLichSuOnTap(lichSu)
            _hoanThanhHoc.value = true
        }
    }
}