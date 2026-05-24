package com.example.learnflash.giaoDien.thongKe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Thành phần giao diện cấu thành toàn bộ màn hình Thống kê
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThongKeUI(viewModel: ThongKeViewModel) {
    // Đọc StateFlow từ ViewModel và chuyển thành State để Compose theo dõi
    val thongKe by viewModel.duLieuThongKe.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Thống kê học tập") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                // Cuộn dọc khi nội dung vượt chiều cao màn hình
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Nhóm thẻ thống kê tổng quan số lượng từ vựng
            Text(
                text = "Tổng quan từ vựng",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Thẻ hiển thị tổng số từ vựng trong cơ sở dữ liệu
            TheThongKe(tieuDe = "Tổng số từ vựng", giaTri = "${thongKe.tongSoTu} từ")

            // Hàng hiển thị song song số từ đã thuộc và chưa thuộc
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Thẻ hiển thị số từ đã được đánh dấu thuộc
                TheThongKe(
                    tieuDe = "Đã thuộc",
                    giaTri = "${thongKe.soTuDaThuoc} từ",
                    modifier = Modifier.weight(1f)
                )
                // Thẻ hiển thị số từ chưa thuộc
                TheThongKe(
                    tieuDe = "Chưa thuộc",
                    giaTri = "${thongKe.soTuChuaThuoc} từ",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Nhóm thẻ thống kê chỉ số SRS (Spaced Repetition System)
            Text(
                text = "Chỉ số SRS (Spaced Repetition)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Thẻ hiển thị giá trị trung bình cấp độ SRS — tính bằng logic Kotlin
            TheThongKe(
                tieuDe = "Trung bình cấp độ",
                giaTri = String.format("%.2f", thongKe.trungBinhCapDoSrs)
            )

            // Hàng hiển thị song song giá trị Min và Max cấp độ SRS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Thẻ hiển thị cấp độ SRS cao nhất (Max)
                TheThongKe(
                    tieuDe = "Cao nhất (Max)",
                    giaTri = "Cấp ${thongKe.caoNhatCapDoSrs}",
                    modifier = Modifier.weight(1f)
                )
                // Thẻ hiển thị cấp độ SRS thấp nhất (Min)
                TheThongKe(
                    tieuDe = "Thấp nhất (Min)",
                    giaTri = "Cấp ${thongKe.thapNhatCapDoSrs}",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Nhóm thẻ thống kê hoạt động ôn tập trong ngày hôm nay
            Text(
                text = "Hoạt động hôm nay",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Hàng hiển thị song song số từ cần ôn và số lượt đã ôn hôm nay
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Thẻ hiển thị số từ tới hạn cần ôn tập trong ngày
                TheThongKe(
                    tieuDe = "Cần ôn hôm nay",
                    giaTri = "${thongKe.soTuOnTapHomNay} từ",
                    modifier = Modifier.weight(1f)
                )
                // Thẻ hiển thị số lượt ôn tập đã thực hiện trong ngày
                TheThongKe(
                    tieuDe = "Đã ôn hôm nay",
                    giaTri = "${thongKe.soLuotDaOnHomNay} lượt",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Thành phần giao diện thẻ thống kê tái sử dụng hiển thị một tiêu đề và một giá trị
@Composable
fun TheThongKe(
    tieuDe: String,
    giaTri: String,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Nhãn mô tả chỉ số thống kê
            Text(
                text = tieuDe,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Giá trị thống kê — hiển thị nổi bật
            Text(
                text = giaTri,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}