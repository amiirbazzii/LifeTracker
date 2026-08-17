package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.Reward
import com.example.ui.theme.*

@Composable
fun RewardsShopTab(
    rewards: List<Reward>,
    userPoints: Int,
    onAddRewardClick: () -> Unit,
    onClaimReward: (Reward) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (rewards.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(DesignTokens.PaddingExtraLarge),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "NO REWARDS YET",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Zinc500,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))
                    Text(
                        text = "Create custom rewards with point costs to motivate your progress.",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = Zinc500,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(DesignTokens.PaddingLarge))
                    Button(
                        onClick = onAddRewardClick,
                        shape = RoundedCornerShape(DesignTokens.PaddingZero),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("add_reward_button_empty")
                    ) {
                        Text("ADD REWARD", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DesignTokens.PaddingLarge, vertical = DesignTokens.PaddingSmall),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AVAILABLE REWARDS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Zinc500
                    )
                    
                    TextButton(
                        onClick = onAddRewardClick,
                        modifier = Modifier.testTag("add_reward_header_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Reward",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(DesignTokens.PaddingTiny))
                        Text(
                            text = "ADD REWARD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = DesignTokens.PaddingLarge),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.PaddingMedium),
                    contentPadding = PaddingValues(top = DesignTokens.PaddingSmall, bottom = 80.dp)
                ) {
                    items(rewards, key = { it.id }) { reward ->
                        RewardCard(
                            reward = reward,
                            canClaim = userPoints >= reward.pointCost,
                            onClaimClick = { onClaimReward(reward) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RewardCard(
    reward: Reward,
    canClaim: Boolean,
    onClaimClick: () -> Unit
) {
    val isAchieved = reward.claimedCount > 0
    val borderColor = if (isAchieved) GridLevel4 else if (canClaim) GridLevel4.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    val cardBg = if (isAchieved) GridLevel4.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
    val borderStroke = if (isAchieved) DesignTokens.StrokeThick else DesignTokens.StrokeMedium

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                borderStroke,
                borderColor,
                RoundedCornerShape(DesignTokens.PaddingZero)
            )
            .background(cardBg)
            .padding(DesignTokens.PaddingMedium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reward.name.uppercase(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (isAchieved) GridLevel4 else MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(DesignTokens.PaddingTiny))
                Text(
                    text = "COST: ${reward.pointCost} PTS  •  CLAIMED: ${reward.claimedCount}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = if (isAchieved) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else Zinc500
                    )
                )
            }

            Spacer(modifier = Modifier.width(DesignTokens.PaddingMedium))

            Button(
                onClick = onClaimClick,
                enabled = !isAchieved && canClaim,
                shape = RoundedCornerShape(DesignTokens.PaddingZero),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canClaim) GridLevel4 else MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                    contentColor = if (canClaim) Color.Black else Zinc500,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                    disabledContentColor = Zinc500
                ),
                border = BorderStroke(
                    DesignTokens.StrokeMedium,
                    if (!isAchieved && canClaim) GridLevel4 else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                ),
                contentPadding = PaddingValues(horizontal = DesignTokens.PaddingMedium, vertical = DesignTokens.PaddingTiny),
                modifier = Modifier.testTag("claim_reward_button_${reward.id}")
            ) {
                Text(
                    text = if (isAchieved) "ACHIEVED" else "CLAIM",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
