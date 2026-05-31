package com.example.learnflash.giaoDien.chiTietTuVung

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnflash.duLieu.khoDuLieu.KhoDuLieuDanhMuc
import com.example.learnflash.duLieu.khoDuLieu.KhoDuLieuTuVung
import com.example.learnflash.duLieu.local.thucThe.DanhMuc
import com.example.learnflash.duLieu.local.thucThe.TuVung
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Lớp ViewModel quản lý luồng State và nghiệp vụ cho màn hình Thêm/Sửa
class ChiTietViewModel(
    private val khoDuLieu: KhoDuLieuTuVung,
    private val khoDuLieuDanhMuc: KhoDuLieuDanhMuc,
    private val idTuVung: Int,
    private val danhMucIdMacDinh: String = "mac_dinh"
) : ViewModel() {

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

    // Trạng thái (State) quản lý ID danh mục đang được chọn cho từ vựng
    private val _danhMucDangChon = mutableStateOf(danhMucIdMacDinh)
    val danhMucDangChon: State<String> = _danhMucDangChon

    // Trạng thái kiểm soát việc hiển thị Loading khi gọi mạng hoặc tải dữ liệu
    private val _dangTai = mutableStateOf(false)
    val dangTai: State<Boolean> = _dangTai

    // Trạng thái lưu trữ chuỗi thông báo lỗi xác thực (Validation)
    private val _loiNhapLieu = mutableStateOf("")
    val loiNhapLieu: State<String> = _loiNhapLieu

    // Luồng StateFlow danh sách danh mục để hiển thị DropdownMenu chọn danh mục
    val danhSachDanhMuc: StateFlow<List<DanhMuc>> = khoDuLieuDanhMuc.layToanBoDanhMuc()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Biến lưu tham chiếu đối tượng gốc để giữ nguyên các trường SRS khi cập nhật
    private var tuVungGoc: TuVung? = null

    init {
        // Tự động tải dữ liệu từ vựng cũ lên form khi đang ở chế độ Sửa (id > 0)
        if (idTuVung > 0) {
            taiDuLieuTuVung()
        }
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
                _danhMucDangChon.value = tuVung.danhMucId
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

    // Hàm cập nhật State khi người dùng chọn danh mục từ DropdownMenu
    fun capNhatDanhMuc(danhMucId: String) {
        _danhMucDangChon.value = danhMucId
    }

    // Thực thi tác vụ gọi 2 API song song: dịch nghĩa tiếng Việt + lấy phiên âm/loại từ
    fun traCuuVaDich() {
        if (_tuKhoa.value.isBlank()) {
            _loiNhapLieu.value = "Vui lòng nhập từ khóa để tra cứu"
            return
        }
        _dangTai.value = true
        viewModelScope.launch {
            val ketQua = khoDuLieu.traCuuVaDich(_tuKhoa.value.trim())
            if (ketQua.isSuccess) {
                val tuVungTraVe = ketQua.getOrNull()
                // Tự động điền kết quả dịch vào các trường State tương ứng
                _nghiaTiengViet.value = tuVungTraVe?.nghiaTiengViet ?: ""
                _phienAm.value = tuVungTraVe?.phienAm ?: ""
                _loaiTu.value = tuVungTraVe?.loaiTu ?: ""
            } else {
                _loiNhapLieu.value = ketQua.exceptionOrNull()?.message ?: "Lỗi kết nối mạng"
            }
            _dangTai.value = false
        }
    }

    // Thực thi xác thực đầu vào và tiến hành lưu dữ liệu qua Repository
    fun luuTuVung(hoanThanh: () -> Unit) {
        // Kiểm tra các trường bắt buộc không được để trống
        if (_tuKhoa.value.isBlank()) {
            _loiNhapLieu.value = "Từ khóa không được để trống"
            return
        }
        if (_nghiaTiengViet.value.isBlank()) {
            _loiNhapLieu.value = "Ý nghĩa không được để trống"
            return
        }
        viewModelScope.launch {
            // Giữ nguyên các trường SRS từ đối tượng gốc khi cập nhật, tránh reset tiến độ học
            val tuVungLuu = TuVung(
                id = idTuVung,
                tuKhoa = _tuKhoa.value.trim(),
                nghiaTiengViet = _nghiaTiengViet.value.trim(),
                phienAm = _phienAm.value.trim(),
                loaiTu = _loaiTu.value.trim(),
                danhMucId = _danhMucDangChon.value,
                capDoSrs = tuVungGoc?.capDoSrs ?: 0,
                ngayOnTapTiepTheo = tuVungGoc?.ngayOnTapTiepTheo ?: System.currentTimeMillis(),
                daThuoc = tuVungGoc?.daThuoc ?: false
            )
            khoDuLieu.luuTuVung(tuVungLuu)
            hoanThanh()
        }
    }
}