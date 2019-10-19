// pages/movie/movie.js

//获得工具变量
const util = require('../../utils/util.js')

Page({

  /**
   * 页面的初始数据
   */

  data: {
    // 被点击的首页导航的菜单的索引
    currentIndexNav: 0,
    // 影视类型
    type: '全部',
    // 每页大小
    pageSize: 24,
    // 当前页数
    pageIndex: 1,
    // 首页导航数据
    navList: ['全部'],
    // 轮播图数据
    swiperList: [],
    // 影视数据
    movieList: []
  },

  /**
   * 跳转到搜索页面
   * @param {*} e 点击对象
   */
  goSearch(e) {
    wx.navigateTo({
      url: '/pages/search-drama/search-drama'
    })
  },

  /**
   * 图片加载时触发的事件
   * @param {*} e 点击对象
   */
  errorFunction(e) {
    var errorImgIndex = e.target.dataset.index                   //获取循环的下标
    var imgObject = "movieList[" + errorImgIndex + "].src"    //commentList为数据源，对象数组
    var errorImg = {}
    errorImg[imgObject] = util.errImg             //构建一个对象
    this.setData(errorImg)                                          //修改数据源对应的数据
  },

  // 点击首页导航按钮
  activeNav(e) {
    var that = this
    var index = e.target.dataset.index
    this.setData({
      currentIndexNav: index,
      type: that.data.navList[index],
      movieList: [],
      pageIndex: 1
    })
    // 加载对应导航类型的数据
    that.onLoad()
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    var that = this
    var movieListParams = util.tvListParams
    movieListParams.type = that.data.type
    movieListParams.pageSize = that.data.pageSize
    movieListParams.pageIndex = that.data.pageIndex
    movieListParams.keyWord = 'null'
    util.getDramaTypeList().then((data) => {
      if (data.code == 0) {
        var dramaTypeWithIdList = data.data
        var dramaTypeList = that.data.navList
        for (var i = 0; i< dramaTypeWithIdList.length; i++) {
          dramaTypeList = dramaTypeList.concat(dramaTypeWithIdList[i].name)
        }
        util.getDramaList(movieListParams).then((data2) => {
          if (data2.code == 0) {
            var movieList = data2.data
            that.setData({
              movieList: that.data.movieList.concat(movieList),
              navList: dramaTypeList,
              type: dramaTypeList[0]
            })
          }
        })
      }
    })
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady: function () {
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow: function () {

  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide: function () {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload: function () {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh: function () {
    var that = this
    wx.showNavigationBarLoading() //在标题栏中显示加载
    that.data.pageIndex = 1 //  设置当前页数为1
    that.data.movieList = [] //  清空影视列表
    that.onLoad()
    wx.hideNavigationBarLoading() //完成停止加载
    wx.stopPullDownRefresh() //停止下拉刷新;
  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom: function () {
    var that = this
    that.data.pageIndex += 1 //  将当前页数加1
    that.onLoad()
  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage: function () {

  },

})