package com.example.learnflash.giaoDien.chiTietTuVung

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnflash.duLieu.khoDuLieu.KhoDuLieuTuVung
import com.example.learnflash.duLieu.local.thucThe.TuVung
import kotlinx.coroutines.launch

// Lớp ViewModel quản lý luồng State và nghiệp vụ cho màn hình Thêm/Sửa
class ChiTietViewModel(
    private val khoDuLieu: KhoDuLieuTuVung,
    private val idTuVung: Int
) : ViewModel() {

    private val LOG_TAG = "ChiTietViewModel_Lifecycle"

    // Trạng thái (State) quản lý chuỗi nhập liệu từ khóa
    private val _tuKhoa = mutableStateOf("")
    val tuKhoa: State<String> = _tuKhoa

    // Trạng thái (State) quản lý chuỗi nhập liệu ý nghĩa tiếng Việt
    private val _nghiaTiengViet = mutableStateOf("")
    val nghiaTiengViet: State<String> = _nghiaTiengViet

    // Trạng thái (State) quản lý chuỗi phiên âm
    private val _phienAm = mutableStateOf("")
    val phienAm: State<String> = _phienAm

    // Trạng thái (State) quản lý chuỗi loại từ
    private val _loaiTu = mutableStateOf("")
    val loaiTu: State<String> = _loaiTu

    // Trạng thái kiểm soát việc hiển thị Loading khi gọi mạng (API) hoặc khi tải dữ liệu
    private val _dangTai = mutableStateOf(false)
    val dangTai: State<Boolean> = _dangTai

    // Trạng thái lưu trữ chuỗi thông báo lỗi xác thực (Validation)
    private val _loiNhapLieu = mutableStateOf("")
    val loiNhapLieu: State<String> = _loiNhapLieu

    // Biến lưu tham chiếu đối tượng gốc để giữ nguyên các trường SRS khi cập nhật
    private var tuVungGoc: TuVung? = null

    init {
        Log.d(LOG_TAG, "ViewModel được khởi tạo (Created) với id: $idTuVung")
        // Tự động tải dữ liệu từ vựng cũ lên form khi đang ở chế độ Sửa (id > 0)
        if (idTuVung > 0) {
            taiDuLieuTuVung()
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(LOG_TAG, "ViewModel bị hủy (Cleared) - Giải phóng tài nguyên")
    }

    // Thực thi truy vấn Room Database để lấy dữ liệu từ vựng theo ID và điền vào State
    private fun taiDuLieuTuVung() {
        _dangTai.value = true
        viewModelScope.launch {
            val tuVung = khoDuLieu.layTuVungTheoId(idTuVung)
            if (tuVung != null) {
                // Lưu tham chiếu gốc để giữ lại capDoSrs, ngayOnTapTiepTheo, daThuoc
                tuVungGoc = tuVung
                // Điền dữ liệu cũ vào từng trường State tương ứng
                _tuKhoa.value = tuVung.tuKhoa
                _nghiaTiengViet.value = tuVung.nghiaTiengViet
                _phienAm.value = tuVung.phienAm
                _loaiTu.value = tuVung.loaiTu
            }
            _dangTai.value = false
        }
    }

    // Hàm cập nhật State khi người dùng nhập từ khóa
    fun capNhatTuKhoa(giaTriMoi: String) {
        _tuKhoa.value = giaTriMoi
        _loiNhapLieu.value = ""
    }

    // Hàm cập nhật State khi người dùng nhập ý nghĩa
    fun capNhatNghia(giaTriMoi: String) {
        _nghiaTiengViet.value = giaTriMoi
        _loiNhapLieu.value = ""
    }

    // Hàm cập nhật State khi người dùng nhập phiên âm
    fun capNhatPhienAm(giaTriMoi: String) {
        _phienAm.value = giaTriMoi
    }

    // Hàm cập nhật State khi người dùng nhập loại từ
    fun capNhatLoaiTu(giaTriMoi: String) {
        _loaiTu.value = giaTriMoi
    }

    // Thực thi tác vụ gọi Repository tra cứu dữ liệu ý nghĩa qua kết nối Internet
    fun traCuuApi() {
        if (_tuKhoa.value.isBlank()) {
            _loiNhapLieu.value = "Vui lòng nhập từ khóa để tra cứu"
            return
        }
        _dangTai.value = true
        viewModelScope.launch {
            val ketQua = khoDuLieu.traCuuTuVungTrucTuyen(_tuKhoa.value)
            if (ketQua.isSuccess) {
                val tuVungApi = ketQua.getOrNull()
                // Tự động điền phiên âm và loại từ từ API vào State tương ứng
                _nghiaTiengViet.value = tuVungApi?.nghiaTiengViet ?: ""
                _phienAm.value = tuVungApi?.phienAm ?: ""
                _loaiTu.value = tuVungApi?.loaiTu ?: ""
            } else {
                _loiNhapLieu.value = ketQua.exceptionOrNull()?.message ?: "Lỗi kết nối mạng"
            }
            _dangTai.value = false
        }
    }

    // Thực thi xác thực đầu vào và tiến hành lưu dữ liệu qua Repository
    fun luuTuVung(hoanThanh: () -> Unit) {
        // --- NHIỆM VỤ TEST: Kiểm tra tính hợp lệ (Validation) ---
        if (_tuKhoa.value.isBlank()) {
            _loiNhapLieu.value = "Từ khóa không được để trống"
            Log.w(LOG_TAG, "Lỗi validation: Từ khóa trống")
            return
        }
        
        if (_tuKhoa.value.length > 50) {
            _loiNhapLieu.value = "Từ khóa không được quá 50 ký tự"
            Log.w(LOG_TAG, "Lỗi validation: Từ khóa quá dài")
            return
        }

        if (_nghiaTiengViet.value.isBlank()) {
            _loiNhapLieu.value = "Ý nghĩa không được để trống"
            Log.w(LOG_TAG, "Lỗi validation: Ý nghĩa trống")
            return
        }
        // --------------------------------------------------------

        viewModelScope.launch {
            Log.d(LOG_TAG, "Bắt đầu lưu từ vựng: ${_tuKhoa.value}")
            // Giữ nguyên các trường SRS từ đối tượng gốc khi cập nhật, tránh reset tiến độ học
            val tuVungLuu = TuVung(
                id = idTuVung,
                tuKhoa = _tuKhoa.value.trim(),
                nghiaTiengViet = _nghiaTiengViet.value.trim(),
                phienAm = _phienAm.value.trim(),
                loaiTu = _loaiTu.value.trim(),
                capDoSrs = tuVungGoc?.capDoSrs ?: 0,
                ngayOnTapTiepTheo = tuVungGoc?.ngayOnTapTiepTheo ?: System.currentTimeMillis(),
                daThuoc = tuVungGoc?.daThuoc ?: false
            )
            khoDuLieu.luuTuVung(tuVungLuu)
            hoanThanh()
        }
    }
}