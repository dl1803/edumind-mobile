package com.edumind.app.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.edumind.app.ui.common.SkeletonBox
import com.edumind.app.ui.theme.BgCanvasMobile

/**
 * Skeleton Loading cho M-06 Trang chủ.
 * Bám sát tuyệt đối theo đặc tả thiết kế trong prototypes/day02/M-06_Home.html (Section 2. SKELETON).
 */
@Composable
fun HomeSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgCanvasMobile)
    ) {
        // 1. Header & Search Bar cố định (Fixed Header bg-white/90 border-b)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.95f))
                .statusBarsPadding()
        ) {
            // Header cố định: Avatar 40dp + 2 dòng text + Chuông 40dp
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar tròn 40dp (w-10 h-10 rounded-full)
                SkeletonBox(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    // Dòng 1: "Xin chào," + Streak (h-3 w-16 mb-1.5)
                    SkeletonBox(
                        modifier = Modifier.size(width = 64.dp, height = 12.dp),
                        shape = RoundedCornerShape(6.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    // Dòng 2: Tên người dùng (h-4 w-28)
                    SkeletonBox(
                        modifier = Modifier.size(width = 112.dp, height = 16.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                // Chuông tròn 40dp (w-10 h-10 rounded-full)
                SkeletonBox(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape
                )
            }

            // Divider dưới Header (border-b border-neutral-100/80)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFF5F5F5))
            )

            // Search Bar Skeleton: Bo tròn capsule 22dp, cao 44dp (h-11 rounded-[22px])
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(22.dp)
                )
            }

            // Divider dưới Search Bar (border-b border-neutral-100/60)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFF5F5F5))
            )
        }

        // 2. Nội dung cuộn dọc (Scrollable Content)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 120.dp)
        ) {
            // Banner Skeleton: 16:9 bo góc 16dp + 4 chấm indicator (px-4 pt-4 pb-2)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                // Page indicator dots (mt-3 gap-1.5)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Chấm 1 active: dải dài 24dp cao 5dp
                    SkeletonBox(
                        modifier = Modifier.size(width = 24.dp, height = 5.dp),
                        shape = RoundedCornerShape(2.5.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // Chấm 2, 3, 4: tròn 5dp
                    SkeletonBox(modifier = Modifier.size(5.dp), shape = CircleShape)
                    Spacer(modifier = Modifier.width(6.dp))
                    SkeletonBox(modifier = Modifier.size(5.dp), shape = CircleShape)
                    Spacer(modifier = Modifier.width(6.dp))
                    SkeletonBox(modifier = Modifier.size(5.dp), shape = CircleShape)
                }
            }

            // Section "Tiếp tục học" Skeleton (px-4 pt-5 pb-1)
            Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
                // Header section: Title 112dp + "Xem tất cả" 64dp
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SkeletonBox(
                        modifier = Modifier.size(width = 112.dp, height = 20.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                    SkeletonBox(
                        modifier = Modifier.size(width = 64.dp, height = 16.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                // Danh sách thẻ ngang cuộn được (w-[265px] h-[98px] rounded-2xl)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SkeletonBox(
                        modifier = Modifier.size(width = 265.dp, height = 98.dp),
                        shape = RoundedCornerShape(16.dp)
                    )
                    SkeletonBox(
                        modifier = Modifier.size(width = 265.dp, height = 98.dp),
                        shape = RoundedCornerShape(16.dp)
                    )
                    SkeletonBox(
                        modifier = Modifier.size(width = 265.dp, height = 98.dp),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            // Section "Gợi ý cho bạn" Skeleton (px-4 pt-6 pb-1)
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)) {
                // Header section: Title 112dp + "Xem tất cả" 64dp
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SkeletonBox(
                        modifier = Modifier.size(width = 112.dp, height = 20.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                    SkeletonBox(
                        modifier = Modifier.size(width = 64.dp, height = 16.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Hàng 1 (2 cards)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CourseCardSkeletonItem(modifier = Modifier.weight(1f))
                    CourseCardSkeletonItem(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Hàng 2 (2 cards)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CourseCardSkeletonItem(modifier = Modifier.weight(1f))
                    CourseCardSkeletonItem(modifier = Modifier.weight(1f))
                }
            }

            // Section "Mới nhất" Skeleton (px-4 pt-6 pb-4)
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                // Header section: Title 80dp + "Xem tất cả" 64dp
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SkeletonBox(
                        modifier = Modifier.size(width = 80.dp, height = 20.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                    SkeletonBox(
                        modifier = Modifier.size(width = 64.dp, height = 16.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Hàng thẻ (2 cards)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CourseCardSkeletonItem(modifier = Modifier.weight(1f))
                    CourseCardSkeletonItem(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Thẻ khóa học Skeleton (Grid 2 cột) bám chuẩn theo M-06_Home.html:
 * - aspect-[16/9] skeleton thumbnail
 * - Padding 12dp:
 *   - Dòng 1: Title full width cao 16dp (h-4 w-full mb-2)
 *   - Dòng 2: Subtitle 75% width cao 12dp (h-3 w-3/4 mb-3)
 *   - Dòng 3: Giá tiền 64dp cao 12dp (h-3 w-16)
 */
@Composable
private fun CourseCardSkeletonItem(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE5E5E5).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    ) {
        Column {
            // Thumbnail 16:9
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            // Khối thông tin p-3 (12dp)
            Column(modifier = Modifier.padding(12.dp)) {
                // Dòng 1: Tên khóa học (h-4 w-full)
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Dòng 2: Giảng viên / Đánh giá (h-3 w-3/4)
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(12.dp),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                // Dòng 3: Giá tiền nằm góc phải
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    SkeletonBox(
                        modifier = Modifier
                            .width(64.dp)
                            .height(12.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }
    }
}
