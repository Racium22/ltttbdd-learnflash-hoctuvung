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

// Định nghĩa các trạng thái học tập của một danh mục
enum class TrangThaiDanhMuc {
    // Danh mục chưa có từ vựng nào
    TRONG,
    // Danh mục có từ vựng nhưng chưa ôn tập từ nào (SRS toàn bộ bằng 0)
    CHUA_ON,
    // Danh mục đang ôn tập (đã ôn ít nhất 1 từ nhưng chưa thuộc hết)
    DANG_ON,
    // Danh mục đã hoàn thành ôn tập (tất cả các từ đều đã thuộc)
    DA_ON
}

// Lớp dữ liệu chứa thông tin hiển thị của danh mục kèm theo trạng thái và thống kê từ vựng
data class DanhMucHienThi(
    val danhMuc: DanhMuc,
    val tongSoTu: Int,
    val soTuDaThuoc: Int,
    val soTuCanOnTap: Int,
    val trangThai: TrangThaiDanhMuc
)

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

    // Trạng thái StateFlow lưu kiểu sắp xếp danh mục (A_Z hoặc Z_A)
    private val _kieuSapXep = MutableStateFlow("A_Z")
    val kieuSapXep: StateFlow<String> = _kieuSapXep.asStateFlow()

    // Trạng thái StateFlow lưu bộ lọc trạng thái ôn tập (TAT_CA, CHUA_ON, DANG_ON, DA_ON)
    private val _boLocTrangThai = MutableStateFlow("TAT_CA")
    val boLocTrangThai: StateFlow<String> = _boLocTrangThai.asStateFlow()

    // Luồng StateFlow danh sách danh mục hiển thị sau khi đã tính toán trạng thái, lọc và sắp xếp
    val danhSachDanhMucHienThi: StateFlow<List<DanhMucHienThi>> = combine(
        khoDuLieuDanhMuc.layToanBoDanhMuc(),
        khoDuLieu.layToanBoTuVung(),
        _kieuSapXep,
        _boLocTrangThai
    ) { danhMucList, tuVungList, sapXep, boLoc ->
        val thoiGianHienTai = System.currentTimeMillis()
        val danhSachMap = danhMucList.map { danhMuc ->
            val tuCuaDanhMuc = tuVungList.filter { it.danhMucId == danhMuc.id }
            val tongSo = tuCuaDanhMuc.size
            val soDaThuoc = tuCuaDanhMuc.count { it.daThuoc }
            val soCanOn = tuCuaDanhMuc.count { !it.daThuoc && it.ngayOnTapTiepTheo <= thoiGianHienTai }

            val trangThai = when {
                tongSo == 0 -> TrangThaiDanhMuc.TRONG
                soDaThuoc == tongSo -> TrangThaiDanhMuc.DA_ON
                tuCuaDanhMuc.all { it.capDoSrs == 0 } -> TrangThaiDanhMuc.CHUA_ON
                else -> TrangThaiDanhMuc.DANG_ON
            }

            DanhMucHienThi(
                danhMuc = danhMuc,
                tongSoTu = tongSo,
                soTuDaThuoc = soDaThuoc,
                soTuCanOnTap = soCanOn,
                trangThai = trangThai
            )
        }

        // Lọc danh sách theo tùy chọn của người dùng
        val danhSachDaLoc = when (boLoc) {
            "CHUA_ON" -> danhSachMap.filter { it.trangThai == TrangThaiDanhMuc.CHUA_ON }
            "DANG_ON" -> danhSachMap.filter { it.trangThai == TrangThaiDanhMuc.DANG_ON }
            "DA_ON" -> danhSachMap.filter { it.trangThai == TrangThaiDanhMuc.DA_ON }
            else -> danhSachMap
        }

        // Sắp xếp danh sách theo tùy chọn của người dùng
        when (sapXep) {
            "A_Z" -> danhSachDaLoc.sortedBy { it.danhMuc.ten.lowercase() }
            "Z_A" -> danhSachDaLoc.sortedByDescending { it.danhMuc.ten.lowercase() }
            else -> danhSachDaLoc
        }
    }.stateIn(
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

    // Thực thi tác vụ bất động bộ kiểm tra Room và tải Firestore nếu cần
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

    // Cập nhật tùy chọn kiểu sắp xếp danh mục
    fun doiKieuSapXep(kieu: String) {
        _kieuSapXep.value = kieu
    }

    // Cập nhật tùy chọn bộ lọc trạng thái ôn tập
    fun doiBoLocTrangThai(loc: String) {
        _boLocTrangThai.value = loc
    }

    // Cho phép người dùng thử tải lại nếu trước đó gặp lỗi
    fun taiLai() {
        _loiKhoiTao.value = ""
        khoiTaoDuLieu()
    }
}