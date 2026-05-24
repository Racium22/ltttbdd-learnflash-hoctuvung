package com.example.learnflash.giaoDien.thongKe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnflash.duLieu.khoDuLieu.KhoDuLieuTuVung
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar

// Lớp dữ liệu (Data Class) đóng gói toàn bộ số liệu thống kê hiển thị lên UI
data class DuLieuThongKe(
    val tongSoTu: Int = 0,
    val soTuDaThuoc: Int = 0,
    val soTuChuaThuoc: Int = 0,
    val trungBinhCapDoSrs: Double = 0.0,
    val caoNhatCapDoSrs: Int = 0,
    val thapNhatCapDoSrs: Int = 0,
    val soTuOnTapHomNay: Int = 0,
    val soLuotDaOnHomNay: Int = 0
)

// Lớp ViewModel quản lý luồng StateFlow dữ liệu thống kê cho màn hình Thống kê
class ThongKeViewModel(
    private val khoDuLieu: KhoDuLieuTuVung
) : ViewModel() {

    // StateFlow phát tán đối tượng thống kê tổng hợp xuống UI
    private val _duLieuThongKe = MutableStateFlow(DuLieuThongKe())
    val duLieuThongKe: StateFlow<DuLieuThongKe> = _duLieuThongKe.asStateFlow()

    init {
        // Kích hoạt thu thập và tổng hợp dữ liệu thống kê ngay khi ViewModel được khởi tạo
        thuThapDuLieuThongKe()
    }

    // Tính mốc thời gian đầu ngày và cuối ngày theo múi giờ thiết bị để truy vấn đúng ngày hôm nay
    private fun tinhKhoangThoiGianHomNay(): Pair<Long, Long> {
        val lichHomNay = Calendar.getInstance()
        // Thiết lập thời điểm bắt đầu ngày: 00:00:00.000
        lichHomNay.set(Calendar.HOUR_OF_DAY, 0)
        lichHomNay.set(Calendar.MINUTE, 0)
        lichHomNay.set(Calendar.SECOND, 0)
        lichHomNay.set(Calendar.MILLISECOND, 0)
        val batDauNgay = lichHomNay.timeInMillis
        // Thiết lập thời điểm cuối ngày: 23:59:59.999
        lichHomNay.set(Calendar.HOUR_OF_DAY, 23)
        lichHomNay.set(Calendar.MINUTE, 59)
        lichHomNay.set(Calendar.SECOND, 59)
        lichHomNay.set(Calendar.MILLISECOND, 999)
        val cuoiNgay = lichHomNay.timeInMillis
        return Pair(batDauNgay, cuoiNgay)
    }

    // Thu thập đồng thời nhiều Flow từ Repository và tổng hợp thành một StateFlow duy nhất
    private fun thuThapDuLieuThongKe() {
        val (batDauNgay, cuoiNgay) = tinhKhoangThoiGianHomNay()
        viewModelScope.launch {
            // Kết hợp 8 Flow song song bằng toán tử combine để tránh nhiều lần Recomposition
            combine(
                khoDuLieu.demTongSoTuVung(),
                khoDuLieu.demSoTuDaThuoc(),
                khoDuLieu.demSoTuChuaThuoc(),
                khoDuLieu.tinhTrungBinhCapDoSrs(),
                khoDuLieu.layCaoNhatCapDoSrs(),
                khoDuLieu.layThapNhatCapDoSrs(),
                khoDuLieu.demTuVungOnTapHomNay(cuoiNgay),
                khoDuLieu.demSoLuotOnTapHomNay(batDauNgay, cuoiNgay)
            ) { mangGiaTri ->
                // Ánh xạ mảng kết quả vào Data Class DuLieuThongKe
                DuLieuThongKe(
                    tongSoTu = mangGiaTri[0] as Int,
                    soTuDaThuoc = mangGiaTri[1] as Int,
                    soTuChuaThuoc = mangGiaTri[2] as Int,
                    trungBinhCapDoSrs = (mangGiaTri[3] as? Double) ?: 0.0,
                    caoNhatCapDoSrs = (mangGiaTri[4] as? Int) ?: 0,
                    thapNhatCapDoSrs = (mangGiaTri[5] as? Int) ?: 0,
                    soTuOnTapHomNay = mangGiaTri[6] as Int,
                    soLuotDaOnHomNay = mangGiaTri[7] as Int
                )
            }.collect { thongKeMoi ->
                // Cập nhật StateFlow để UI tự động Recompose khi dữ liệu thay đổi
                _duLieuThongKe.value = thongKeMoi
            }
        }
    }
}