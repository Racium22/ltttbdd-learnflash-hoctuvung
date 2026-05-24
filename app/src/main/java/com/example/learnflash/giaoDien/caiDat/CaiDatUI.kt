package com.example.learnflash.giaoDien.caiDat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Thành phần giao diện cấu thành màn hình Cài đặt ứng dụng
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaiDatUI(
    viewModel: CaiDatViewModel,
    quayLai: () -> Unit
) {
    // Đọc StateFlow từ ViewModel và chuyển thành State để Compose theo dõi thay đổi
    val giaoDienToi by viewModel.giaoDienToi.collectAsState()
    val mucTieuHocNgay by viewModel.mucTieuHocNgay.collectAsState()

    // Khung giao diện Scaffold với TopAppBar có nút quay lại
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt") },
                navigationIcon = {
                    // Nút điều hướng quay lại màn hình trước
                    IconButton(onClick = quayLai) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Nhóm cài đặt giao diện hiển thị
            Text(
                text = "Giao diện",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Hàng điều khiển bật/tắt giao diện tối bằng Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    // Nhãn tên cài đặt giao diện tối
                    Text(
                        text = "Giao diện tối (Dark Mode)",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    // Mô tả phụ cho cài đặt
                    Text(
                        text = "Chuyển màu nền sang tối",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Switch kết nối trực tiếp với StateFlow giaoDienToi — thay đổi lưu ngay vào DataStore
                Switch(
                    checked = giaoDienToi,
                    onCheckedChange = { viewModel.doiGiaoDienToi(it) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Nhóm cài đặt mục tiêu học tập
            Text(
                text = "Mục tiêu học tập",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Nhãn hiển thị giá trị mục tiêu học từ hiện tại
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Số từ ôn tập mỗi ngày",
                    style = MaterialTheme.typography.bodyLarge
                )
                // Hiển thị giá trị số từ mục tiêu nổi bật
                Text(
                    text = "$mucTieuHocNgay từ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Thanh trượt Slider điều chỉnh mục tiêu từ 5 đến 50 từ mỗi ngày
            Slider(
                value = mucTieuHocNgay.toFloat(),
                onValueChange = { viewModel.doiMucTieuHocNgay(it.toInt()) },
                valueRange = 5f..50f,
                steps = 8,
                modifier = Modifier.fillMaxWidth()
            )

            // Nhãn hiển thị khoảng giá trị hai đầu của Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "5 từ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "50 từ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}