<template>
	<view class="container">
		<view class="trtc-calling-index">
			<view class="trtc-calling-index-search">
				<view class="search">
					<view class="input-box">
						<input class="input-search-user" :value="userIDToSearch" maxlength="50" type="text"
							v-on:input="userIDToSearchInput" :placeholder="$t('Search User ID')" />
					</view>
					<view class="btn-search" @click="searchUser">{{ $t('Search') }}</view>
				</view>
				<view class="search-selfInfo">
					<label class="search-selfInfo-label">{{ $t('Your ID') }}</label>
					<view class="search-selfInfo-phone">
						{{config.userID}}
					</view>
				</view>
				<view class="search-result">
					<view v-if="invitee.userID" class="user-to-call">
						<view class="userInfo-box">
							<image class="userInfo-avatar" :src="invitee.avatar"></image>
							<text class="userInfo-name">{{ invitee.userID}}</text>
						</view>
						<view class="btn-userinfo-call" @click="call">{{ $t('Call') }}</view>
					</view>
					<view v-else>{{$t('User not found')}}</view>
				</view>
				<view v-if="!invitee.userID" class="search-default">
					<view class="search-default-box">
						<image class="search-default-img" src="../../static/search.png" lazy-load="true" />
						<view class="search-default-message">
							{{ $t('initiated a call') }}
						</view>
					</view>
				</view>
			</view>
			
		</view>
	</view>

</template>

<script>
	export default {
		data() {
			return {
				userIDToSearch: '',
				searchResultShow: false,
				invitee: {
					userID: ''
				},
				config: {
					sdkAppID: '',
					userID: '',
					userSig: '',
					type: 1, // 语音通话(callMediaType = 1)、视频通话(callMediaType = 2)
				},
			}
		},
		onLoad(option) {
			this.config = {
				sdkAppID: getApp().globalData.SDKAppID,
				userID: getApp().globalData.userID,
				userSig: getApp().globalData.userSig,
				type: Number(option.type)
			}
			console.log(this.config);
		},
		methods: {
			userIDToSearchInput(e) {
				this.userIDToSearch = e.detail.value
			},
			searchUser() {
				// 去掉前后空格
				const newSearch = this.userIDToSearch.trim();
				this.userIDToSearch = newSearch
				this.invitee = this.userIDToSearch
				uni.$TUIKit.getUserProfile({
						userIDList: [this.userIDToSearch]
					})
					.then((imResponse) => {
						if (imResponse.data.length === 0) {
							uni.showToast({
								title: this.$t('User not found'),
								icon: 'none',
							});
							return;
						}
						this.invitee = {
							...imResponse.data[0]
						}
						this.searchResultShow = true
					});
			},
			call() {
				if (this.config.userID === this.invitee.userID) {
					uni.showToast({
						icon: 'none',
						title: this.$t('Do not call local'),
					});
					return;
				}
				try {
					// type：通话的媒体类型，比如：语音通话(callMediaType = 1)、视频通话(callMediaType = 2)
					const callParams = {
						userIDList: [this.invitee.userID],
						callMediaType: this.config.type,
						// callParams: {
						// 	roomID: 0,
						// 	timeout:30,
						// 	offlinePushInfo: {
						// 		title: "test-title",
						// 		description: "you have a call",
						// 		androidSound: "rain",
						// 		iOSSound: "rain.mp3",
						// 	},
						// 	userData:'testuserData'
						// },
						
					};
					console.log('--> ', JSON.stringify(callParams));
					uni.$TUICallKit.calls(callParams,
						res => {
							console.log(JSON.stringify(res));
						}
					);
				} catch (error) {
					uni.showToast({
						title: this.$t('call failure'),
						icon: "none"
					})
				}
			},
			// 手动挂断
			handleHangup() {
				uni.$TUICallEngine.hangup();
			},
			// 设置渲染模式参数
			handleSetVideoRenderParams() {
				const params = {
					userID: this.config.userID,
					fillMode: 0, // 0-填充模式，1一适应模式
					rotation: 1, // 0~3分别对应值：Rotation_0, Rotation_90, Rotation_180, Rotation_270;
				};
				uni.$TUICallEngine.setVideoRenderParams(params, (res) => {
					console.warn('渲染设置回调 = ', JSON.stringify(res));
				});
			},
			// 设置采集参数
			handleSetVideoEncoderParams() {
				const params = {
					resolution: 110,
					resolutionMode: 0, // 0-Landscape(横屏)，1一Portrait(竖屏)
				};
				uni.$TUICallEngine.setVideoEncoderParams(params, (res) => {
					console.warn('采集参数回调 = ', JSON.stringify(res));
				});
			},
		}
	}
</script>

<style>
	.container {
		width: 100vw;
		height: 100vh;
		overflow: hidden;
		/* background-image: url(https://mc.qcloudimg.com/static/img/7da57e0050d308e2e1b1e31afbc42929/bg.png); */
		margin: 0;
	}

	.trtc-calling-index {
		width: 100vw;
		height: 100vh;
		color: white;
		display: flex;
		flex-direction: column;
	}

	.trtc-calling-index-title {
		position: relative;
		display: flex;
		width: 100%;
		margin-top: 3.8vh;
		justify-content: center;
	}

	.trtc-calling-index-title .title {
		margin: 0;
		font-family: PingFangSC-Regular;
		font-size: 16px;
		color: #000000;
		letter-spacing: 0;
		line-height: 36px;
		padding: 1.2vh;
	}

	.btn-goback {
		position: absolute;
		left: 2vw;
		top: 1.2vh;
		width: 8vw;
		height: 8vw;
		z-index: 9;
	}

	.trtc-calling-index-search {
		flex: 1;
		margin: 0;
		display: flex;
		flex-direction: column;
	}

	.trtc-calling-index-search>.search {
		width: 100%;
		display: flex;
		justify-content: space-between;
		align-items: center;
		box-sizing: border-box;
		padding: 16px;
	}

	.btn-search {
		text-align: center;
		width: 60px;
		height: 40px;
		line-height: 40px;
		background: #0A6CFF;
		border-radius: 20px;
	}

	.search-result {
		width: 90%;
		height: 40px;
		margin-left: 5%;
	}

	.input-box {
		flex: 1;
		box-sizing: border-box;
		margin-right: 20px;
		height: 40px;
		background: #F4F5F9;
		color: #666666;
		border-radius: 20px;
		padding: 9px 16px;
		display: flex;
		align-items: center;
	}

	.icon-right {
		width: 8px;
		height: 12px;
		margin: 0 4px;
	}

	.input-search-user {
		flex: 1;
		box-sizing: border-box;
	}

	.input-label {
		display: flex;
		align-items: center;
	}

	.input-label-plus {
		padding-bottom: 3px;
	}

	.input-search-user[placeholder] {
		font-family: PingFangSC-Regular;
		font-size: 16px;
		color: #8A898E;
		letter-spacing: 0;
		font-weight: 400;
	}

	.user-to-call {
		display: flex;
		justify-content: space-between;
		width: 100%;
		height: 50px;
		margin: 0;
		padding: 16px 0;
	}

	.userInfo-box {
		display: flex;
		align-items: center;
		font-size: 12px;
		color: #333333;
		letter-spacing: 0;
		font-weight: 500;
	}

	.userInfo-box>.userInfo-avatar {
		width: 50px;
		height: 50px;
		border-radius: 50px;
	}

	.userInfo-box>.userInfo-name {
		padding-left: 8px;
	}

	.btn-userinfo-call {
		width: 60px;
		height: 40px;
		text-align: center;
		background: #0A6CFF;
		border-radius: 20px;
		line-height: 40px;
		margin: 10px 0;
		color: rgba(255, 255, 255);
	}

	.user-to-call>image {
		height: 50px;
		line-height: 50px;
		border-radius: 50px
	}

	/* .title-number {
	height: 20px;
	margin-top: 3%;
	padding: 5px 0;
	border-top: 1px solid rgba(255, 255, 255, 0.11);
	font-size: 12px;
} */
	.search-selfInfo {
		position: relative;
		display: flex;
		align-items: center;
		padding: 0 28px;
		font-family: PingFangSC-Regular;
		font-size: 14px;
		color: #333333;
		letter-spacing: 0;
		font-weight: 400;
	}

	.search-selfInfo::before {
		position: absolute;
		content: "";
		width: 4px;
		height: 12px;
		background: #9A9A9A;
		border: 1px solid #979797;
		border-radius: 2px;
		margin: auto 0;
		left: 16px;
		top: 0;
		bottom: 0;
	}

	.search-selfInfo-phone {
		padding-left: 8px;
	}


	.incoming-call {
		width: 100vw;
		height: 100vh;
	}

	.btn-operate {
		display: flex;
		justify-content: space-between;
		position: absolute;
		bottom: 5vh;
		width: 100%;
	}

	.call-operate {
		width: 15vw;
		height: 15vw;
		border-radius: 15vw;
		padding: 5px;
		margin: 0 15vw;
	}

	.tips {
		width: 100%;
		height: 40px;
		line-height: 40px;
		text-align: center;
		font-size: 20px;
		color: #333333;
		letter-spacing: 0;
		font-weight: 500;
	}

	.tips-subtitle {
		height: 20px;
		font-family: PingFangSC-Regular;
		font-size: 14px;
		color: #97989C;
		letter-spacing: 0;
		font-weight: 400;
		text-align: center;
	}


	.search-default {
		flex: 1;
		display: flex;
		align-items: flex-end;
		justify-content: center;
	}

	.search-default-box {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding-bottom: 50vh;
	}

	.search-default-img {
		width: 64px;
		height: 66px;
	}

	.search-default-message {
		width: 126px;
		padding: 16px;
		font-family: PingFangSC-Regular;
		font-size: 14px;
		color: #8A898E;
		letter-spacing: 0;
		text-align: center;
		font-weight: 400;
	}
</style>
