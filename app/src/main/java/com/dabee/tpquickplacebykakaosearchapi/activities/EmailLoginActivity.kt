package com.dabee.tpquickplacebykakaosearchapi.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.dabee.tpquickplacebykakaosearchapi.G
import com.dabee.tpquickplacebykakaosearchapi.R
import com.dabee.tpquickplacebykakaosearchapi.databinding.ActivityEmailLoginBinding
import com.dabee.tpquickplacebykakaosearchapi.model.UserAccount
import com.google.firebase.firestore.FirebaseFirestore

class EmailLoginActivity : AppCompatActivity() {

    private val binding by lazy { ActivityEmailLoginBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //툴바에 업버튼 만들기
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)

        binding.btnSignIn.setOnClickListener { clickSignIn() }
    }

    //업버튼 클릭시에 액티비티를 종료
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun clickSignIn(){

        var email = binding.etEmail.text.toString()
        var password = binding.etPassword.text.toString()

        // Firebase Firestore DB 에서 이메일 로그인 여부 확인
        val db = FirebaseFirestore.getInstance()

        db.collection("emailUsers")
            .whereEqualTo("email",email)
            .whereEqualTo("password",password)
            .get().addOnSuccessListener {
                if(it.documents.size> 0){ //where 조건에 맞는 데이터가 있다는 것
                    // 로그인 성공 [ 회원정보를 다른 Activity 에서도 사용할 가능성 있으므러 전역변수 처럼 클래스 이름만으로 사용가능한 변수에 저장하기 ]
                    var id = it.documents[0].id //document 의 랜덤한 식별자
                    G.userAccount = UserAccount(id,email)
                    
                    // 로그인 성공 했으니 곧바로 MainActivity 로 이동

                    // 다른액티비티로 넘어가면서 task에 있는 모든 액티비티들을 제거하고 새로운 task로 시작
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                    startActivity(Intent(this,MainActivity::class.java))


                }else{
                    // 로그인 실패
                    AlertDialog.Builder(this).setMessage("이메일과 비밀번호를 다시 확인해주세요.").show()
                    binding.etEmail.requestFocus()
                    binding.etEmail.selectAll()
                }


            }.addOnFailureListener {
                Toast.makeText(this, "서버오류 : ${it.message}", Toast.LENGTH_SHORT).show()
            }

    }


}