package com.example.reunion.view_model

import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.example.reunion.MyApplication
import com.example.reunion.R
import com.example.reunion.base.BaseViewModel
import com.example.reunion.repostory.bean.NewsBean
import com.example.reunion.repostory.remote_resource.NewsRemoteModel
import com.example.reunion.view.NewsContentFragment
import retrofit2.HttpException
import java.net.UnknownHostException

class NewsViewModel: BaseViewModel() {
    private val remoteModel:NewsRemoteModel by lazy { NewsRemoteModel() }
    var contentTypeIndex = 0
    var currentIndex = 0
    val news:MutableLiveData<ArrayList<NewsBean.News>> = MutableLiveData(ArrayList())
    val isRefreshEnd:MutableLiveData<Boolean> = MutableLiveData(true)
    val isLoadEnd:MutableLiveData<Boolean> = MutableLiveData(true)
    val clearList:MutableLiveData<Boolean> = MutableLiveData(false)
    private var page:Int = 0

    val healthFragment = NewsContentFragment.getInstance(NewsContentFragment.HEALTHY)
    val childFragment = NewsContentFragment.getInstance(NewsContentFragment.Child)
    val publicFragment = NewsContentFragment.getInstance(NewsContentFragment.PUBLIC_WELFARE)

    fun onLoadNews(view: View? = null){
        if (isLoadEnd.value == false)
            return
        if ((contentTypeIndex == NewsContentFragment.PUBLIC_WELFARE && page != 0)||page == 440){
            toast.value = MyApplication.app.resources.getString(R.string.load_all)
            isLoadEnd.value = true
            return
        }
        launch ({
            isLoadEnd.value = false
            val bean:NewsBean = when(contentTypeIndex){
                NewsContentFragment.HEALTHY->{
                    remoteModel.getHealthyNews(page)
                }
                NewsContentFragment.Child->{
                    remoteModel.getChildNews(page)
                }
                else->{
                    remoteModel.getPublicNews()
                }
            }
            when(bean.status){
                0->{
                    page+=40
                    val list = (bean.result as NewsBean.Result).list!!
                    for (index in (list.size -1) downTo 0){
                        list[index].src = list[index].src.replace(" ","")
                        list[index].src = list[index].src.replace("\n","")
                        if (list[index].content.contains("video")||list[index].content.length < 100){
                            list.remove(list[index])
                        }
                    }
                    news.value = list
                }
            }
        },{
            if (it is HttpException || it is UnknownHostException)
                toast.value = it.message
            else
                toast.value = MyApplication.resource().getString(R.string.fail_load)
        },{
            isLoadEnd.value = true
        })
    }

    fun onRefresh(view: View? = null){
        if (isRefreshEnd.value == false)
            return
        page = 0
        isRefreshEnd.value = false
        launch ({
            val bean:NewsBean = when(contentTypeIndex){
                NewsContentFragment.HEALTHY->{
                    remoteModel.getHealthyNews(page)
                }
                NewsContentFragment.Child->{
                    remoteModel.getChildNews(page)
                }
                else->{
                    remoteModel.getPublicNews()
                }
            }
            when(bean.status){
                0->{
                    clearList.value = true
                    page+=40
                    news.value = (bean.result as NewsBean.Result).list!!
                }
            }
        },{
            if (it is HttpException || it is UnknownHostException)
                toast.value = it.message
            else
                toast.value = "刷新错误"
        },{
            isRefreshEnd.value = true
        })
    }
}