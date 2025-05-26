package com.example.aisupabase.ui.theme


import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),    // dùng cho nút, chip nhỏ
    medium = RoundedCornerShape(12.dp),  // dùng cho thẻ, hộp thoại
    large = RoundedCornerShape(24.dp)    // dùng cho card lớn, container
)
