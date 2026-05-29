package com.example.learnflash.duLieu.local.thucThe

import androidx.room.Entity
import androidx.room.PrimaryKey

// Bảng dữ liệu lưu trữ danh mục chủ đề từ vựng trong Room Database
@Entity(tableName = "danh_muc")
data class DanhMuc(
    // Khóa chính dạng chuỗi để đồng bộ ID với Firestore sau này
    @PrimaryKey
    val id: String,
    // Tên hiển thị của danh mục chủ đề
    val ten: String,
    // Mô tả ngắn về nội dung danh mục
    val moTa: String = "",
    // Đánh dấu danh mục thuộc bộ 3000 từ mặc định — không cho phép xóa
    val laMacDinh: Boolean = false
)