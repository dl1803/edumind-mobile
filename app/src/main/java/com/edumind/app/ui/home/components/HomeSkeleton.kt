package com.edumind.app.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edumind.app.ui.common.SkeletonBox

@Composable
fun HomeSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 96.dp)
    ) {
        // Header Skeleton
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonBox(modifier = Modifier.size(42.dp), shape = CircleShape)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                SkeletonBox(modifier = Modifier.size(width = 80.dp, height = 12.dp))
                Spacer(modifier = Modifier.height(6.dp))
                SkeletonBox(modifier = Modifier.size(width = 140.dp, height = 16.dp))
            }
            SkeletonBox(modifier = Modifier.size(36.dp), shape = CircleShape)
        }

        // Search Bar Skeleton
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(22.dp)
            )
        }

        // Banner Skeleton (16:9)
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Continue Learning Skeleton
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SkeletonBox(modifier = Modifier.size(width = 120.dp, height = 18.dp))
                SkeletonBox(modifier = Modifier.size(width = 60.dp, height = 14.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SkeletonBox(
                    modifier = Modifier.size(width = 265.dp, height = 94.dp),
                    shape = RoundedCornerShape(16.dp)
                )
                SkeletonBox(
                    modifier = Modifier.size(width = 265.dp, height = 94.dp),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        // Course Grid Skeleton (2 cột)
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            SkeletonBox(modifier = Modifier.size(width = 130.dp, height = 18.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(200.dp),
                    shape = RoundedCornerShape(16.dp)
                )
                SkeletonBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(200.dp),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}
