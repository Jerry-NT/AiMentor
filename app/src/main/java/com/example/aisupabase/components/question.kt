package com.example.aisupabase.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults.outlinedTextFieldColors
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object question {
    @Composable
    fun Option_ABCD(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
        val backgroundColor = if (isSelected) {
            Color(0xFF4ECDC4).copy(alpha = 0.1f)
        } else {
            Color.Transparent
        }

        val borderColor = if (isSelected) {
            Color(0xFF4ECDC4)
        } else {
            Color(0xFFE2E8F0)
        }

        val textColor = if (isSelected) {
            Color(0xFF2D5A5A)
        } else {
            Color(0xFF4A5568)
        }

        Surface(
            modifier = modifier.clickable { onClick() },
            shape = RoundedCornerShape(16.dp),
            color = backgroundColor,
            border = androidx.compose.foundation.BorderStroke(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    fontSize = 18.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Option_input(
        value: String,
        onValueChange: (String) -> Unit,
        placeholder: String,
        keyboardType: KeyboardType,
        maxLength: Int,
        focusRequester: FocusRequester,
        onDone: () -> Unit,
        modifier: Modifier = Modifier) {
        Column(modifier = modifier) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(
                        text = placeholder,
                        color = Color.Gray
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                shape = RoundedCornerShape(16.dp),
            colors = outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF4ECDC4),
                unfocusedBorderColor = Color(0xFFE2E8F0),
                cursorColor = Color(0xFF4ECDC4)
            ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onDone() }
                ),
                singleLine = keyboardType != KeyboardType.Text || maxLength <= 100,
                maxLines = if (keyboardType == KeyboardType.Text && maxLength > 100) 3 else 1
            )

            // Character counter
            if (maxLength > 0) {
                Text(
                    text = "${value.length}/$maxLength",
                    fontSize = 12.sp,
                    color = if (value.length >= maxLength) Color.Red else Color.Gray,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp)
                )
            }
        }
    }
}