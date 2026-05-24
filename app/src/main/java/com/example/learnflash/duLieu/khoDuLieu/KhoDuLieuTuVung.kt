package com.example.learnflash.duLieu.khoDuLieu

import com.example.learnflash.duLieu.local.dao.LichSuOnTapDao
import com.example.learnflash.duLieu.local.dao.TuVungDao
import com.example.learnflash.duLieu.local.thucThe.LichSuOnTap
import com.example.learnflash.duLieu.local.thucThe.TuVung
import com.example.learnflash.duLieu.remote.api.TuVungApi
import kotlinx.coroutines.flow.Flow

// Lớp Repository xử lý nghiệp vụ điều phối dữ liệu giữa Local Database và Remote API
class KhoDuLieuTuVung(
    private val tuVungDao: TuVungDao,
    private val lichSuOnTapDao: LichSuOnTapDao,
    private val tuVungApi: TuVungApi
) {

    // Lấy toàn bộ từ vựng dưới dạng luồng dữ liệu (Flow) để UI phản ứng với thay đổi
    fun layToanBoTuVung(): Flow<List<TuVung>> = tuVungDao.layToanBoTuVung()

    // Lấy danh sách từ vựng tới hạn ôn tập dựa trên thời gian hệ thống
    fun layTuVungCanOnTap(thoiGianHienTai: Long): Flow<List<TuVung>> =
        tuVungDao.layTuVungCanOnTap(thoiGianHienTai)

    // Truy vấn một từ vựng theo ID để phục vụ màn hình Sửa
    suspend fun layTuVungTheoId(id: Int): TuVung? = tuVungDao.layTuVungTheoId(id)

    // Thực thi thao tác thêm hoặc cập nhật từ vựng vào Room Database
    suspend fun luuTuVung(tuVung: TuVung) {
        if (tuVung.id == 0) {
            tuVungDao.themTuVung(tuVung)
        } else {
            tuVungDao.capNhatTuVung(tuVung)
        }
    }

    // Thực thi thao tác xóa từ vựng khỏi Room Database
    suspend fun xoaTuVung(tuVung: TuVung) = tuVungDao.xoaTuVung(tuVung)

    // Thực thi thao tác tra cứu ý nghĩa trực tuyến qua Coroutines
    suspend fun traCuuTuVungTrucTuyen(tuKhoa: String): Result<TuVung> {
        return try {
            // Gọi HTTP GET để nhận phản hồi từ API
            val phanHoi = tuVungApi.traCuuTuVung(tuKhoa)
            if (phanHoi.isSuccessful && !phanHoi.body().isNullOrEmpty()) {
                val duLieuRemote = phanHoi.body()!![0]
                // Trích xuất loại từ và ý nghĩa từ cấu trúc cây JSON
                val loaiTu = duLieuRemote.danhSachNghia.firstOrNull()?.loaiTu ?: ""
                val yNghia = duLieuRemote.danhSachNghia.firstOrNull()?.danhSachDinhNghia?.firstOrNull()?.dinhNghia ?: ""
                val tuVungMoi = TuVung(
                    tuKhoa = duLieuRemote.tuKhoa,
                    phienAm = duLieuRemote.phienAm ?: "",
                    loaiTu = loaiTu,
                    nghiaTiengViet = yNghia
                )
                Result.success(tuVungMoi)
            } else {
                Result.failure(Exception("Không tìm thấy thông tin từ vựng trên từ điển trực tuyến."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Các hàm phục vụ Màn hình Thống kê ---

    // Trả về luồng dữ liệu tổng số từ vựng
    fun demTongSoTuVung(): Flow<Int> = tuVungDao.demTongSoTuVung()

    // Trả về luồng dữ liệu số từ đã thuộc
    fun demSoTuDaThuoc(): Flow<Int> = tuVungDao.demSoTuDaThuoc()

    // Trả về luồng dữ liệu số từ chưa thuộc
    fun demSoTuChuaThuoc(): Flow<Int> = tuVungDao.demSoTuChuaThuoc()

    // Trả về luồng dữ liệu giá trị trung bình cấp độ SRS
    fun tinhTrungBinhCapDoSrs(): Flow<Double?> = tuVungDao.tinhTrungBinhCapDoSrs()

    // Trả về luồng dữ liệu giá trị cấp độ SRS cao nhất
    fun layCaoNhatCapDoSrs(): Flow<Int?> = tuVungDao.layCaoNhatCapDoSrs()

    // Trả về luồng dữ liệu giá trị cấp độ SRS thấp nhất
    fun layThapNhatCapDoSrs(): Flow<Int?> = tuVungDao.layThapNhatCapDoSrs()

    // Trả về luồng dữ liệu số từ vựng cần ôn tập trong ngày hôm nay
    fun demTuVungOnTapHomNay(cuoiNgayHomNay: Long): Flow<Int> =
        tuVungDao.demTuVungOnTapHomNay(cuoiNgayHomNay)

    // Trả về luồng dữ liệu số lượt ôn tập đã hoàn thành trong ngày hôm nay
    fun demSoLuotOnTapHomNay(batDauNgay: Long, cuoiNgay: Long): Flow<Int> =
        lichSuOnTapDao.demSoLuotOnTapTrongNgay(batDauNgay, cuoiNgay)

    // Lấy luồng dữ liệu toàn bộ lịch sử ôn tập
    fun layToanBoLichSu(): Flow<List<LichSuOnTap>> = lichSuOnTapDao.layToanBoLichSu()

    // Ghi nhận một phiên ôn tập mới vào Room Database
    suspend fun themLichSuOnTap(lichSu: LichSuOnTap) = lichSuOnTapDao.themLichSu(lichSu)
}