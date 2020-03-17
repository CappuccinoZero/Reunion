package com.example.reunion.view

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.reunion.R
import com.example.reunion.base.BaseActivity
import com.example.reunion.databinding.ActivityCommunityBinding
import com.example.reunion.databinding.DialogCommunityBinding
import com.example.reunion.repostory.bean.CommunityBean
import com.example.reunion.util.ViewUtil
import com.example.reunion.view.adapter.CommunityCommentAdapter
import com.example.reunion.view_model.CommunityViewModel

class CommunityActivity : BaseActivity() {

    private val adapter = CommunityCommentAdapter{type,comment ->
        run {
            when (type) {
                CommunityCommentAdapter.SHOW_EDIT -> {
                    mBinding.communityEdit.hint = "回复 ${comment?.nickName} :"
                    mViewModel.setEditText(comment?.floor?:0,comment?.uId).observe(this, Observer {
                        mBinding.communityEdit.setSelection(mBinding.communityEdit.text.toString().length)
                    })
                    ViewUtil.showInput(this,mBinding.communityEdit)
                }
                CommunityCommentAdapter.LOAD_COMMENT -> {
                    mViewModel.onLoadingComment()
                }
            }
        }
    }

    private val mViewModel by lazy { setViewModel(this,CommunityViewModel::class.java) }

    private lateinit var mBinding:ActivityCommunityBinding
    private lateinit var mDialogBinding:DialogCommunityBinding

    private lateinit var deleteDialog:Dialog

    private val dialog by lazy {
        val dialog =  Dialog(this,R.style.CustomizeDialog)
        dialog.setContentView(mDialogBinding.root)

        val window = dialog.window
        val layoutParams = window?.attributes
        layoutParams?.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams?.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams?.gravity = Gravity.BOTTOM
        window?.setWindowAnimations(R.style.DialogAnim)
        dialog.show()
        dialog.dismiss()
        dialog.setCanceledOnTouchOutside(true)
        dialog
    }

    override fun create(savedInstanceState: Bundle?) {
        mBinding = DataBindingUtil.setContentView(this,R.layout.activity_community)
        mBinding.lifecycleOwner = this
        mBinding.activity = this
        mBinding.viewModel = mViewModel

        mDialogBinding = DataBindingUtil.inflate(layoutInflater,R.layout.dialog_community,null,false)
        mDialogBinding.activity = this

        initData()
        initView()
        initViewModel()
    }

    private fun initData(){
        val data = intent.getParcelableExtra<CommunityBean>("data")
        if (data != null){
            mViewModel.initData(data)
        }else{
            toast("获取内容失败")
        }
    }

    private fun initView(){
        val manager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        mBinding.recyclerView.layoutManager = manager
        mBinding.recyclerView.adapter = adapter

        deleteDialog = ViewUtil.createNormalDialog(this,
            "是否确定删除您发布的内容？删除后无法再恢复。","删除",{
                deleteDialog.dismiss()
                mViewModel.onDeleteCommunity()
            },{
                deleteDialog.dismiss()
            })
    }

    private fun initViewModel(){
        mViewModel.comments.observe(this, Observer {
            for (item in it){
                val position = adapter.comments.size
                adapter.comments.add(item)
                adapter.notifyItemInserted(position)
            }
        })

        mViewModel.loading.observe(this, Observer {
            adapter.type = it
            adapter.notifyItemChanged(adapter.comments.size)
        })

        mViewModel.comment.observe(this, Observer {
            adapter.comments.add(it)
            adapter.notifyItemInserted(adapter.comments.size - 1)
            mBinding.recyclerView.smoothScrollToPosition(adapter.itemCount)
            ViewUtil.hideInput(this)
        })

        mViewModel.deleteResult.observe(this, Observer {
            if (it){
                finish()
            }
        })
    }

    fun onBack(view: View) = onBackPressed()

    fun onClickShowDialog(view: View) = dialog.show()

    fun onCancelDialog(view: View) = dialog.dismiss()

    fun onDeleteCommunity(view: View){
        dialog.dismiss()
        deleteDialog.show()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (super.dispatchTouchEvent(ev)){
            if (ev?.y?.toInt()?:0 < mBinding.imEditLayout.top ){
                mBinding.communityEdit.hint = resources.getString(R.string.comment_edit)
                mViewModel.setEditText().observe(this, Observer {
                    mBinding.communityEdit.setSelection(mBinding.communityEdit.text.toString().length)
                })
                ViewUtil.hideInput(this)
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}