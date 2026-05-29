package com.example.learnflash.duLieu.remote.moHinhReMote

import com.google.gson.annotations.SerializedName

// Lớp dữ liệu hứng toàn bộ phản hồi JSON từ MyMemory Translation API
data class MoHinhDichThuat(
    // Ánh xạ khối dữ liệu kết quả dịch chính
    @SerializedName("responseData") val duLieuKetQua: DuLieuKetQuaDich,
    // Ánh xạ mã trạng thái phản hồi (200 = thành công)
    @SerializedName("responseStatus") val maKetQua: Int
)

// Lớp dữ liệu chứa chuỗi văn bản đã được dịch
data class DuLieuKetQuaDich(
    // Ánh xạ chuỗi kết quả dịch thuật từ ngôn ngữ nguồn sang ngôn ngữ đích
    @SerializedName("translatedText") val vanBanDaDich: String
)