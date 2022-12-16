package com.dabee.tpquickplacebykakaosearchapi.activities

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.dabee.tpquickplacebykakaosearchapi.R
import com.dabee.tpquickplacebykakaosearchapi.databinding.ActivitySignupBinding
import com.google.firebase.firestore.FirebaseFirestore


class SignupActivity : AppCompatActivity() {

    val binding by lazy { ActivitySignupBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        //툴바를 액션바로 설정
        setSupportActionBar(binding.toolbar)

        //액션바에 업버튼 설정 및 제목글씨 제거
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)

        binding.btnSignup.setOnClickListener { clickSignup() }

    }

    // 업버튼 클릭할때 자동 발동하는 콜백메소드
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun clickSignup(){
        // Firebase Firestore DB에 사용자 정보 저장하기 [앱과 firebase 플랫폼 연동]

        var email:String = binding.etEmail.text.toString()
        var password:String = binding.etPassword.text.toString()
        var passwordConfirm = binding.etPasswordConfirm.text.toString()

        // 원래는 정규표현식(RegExp)을 이용하여 유효성 검사함. 시간상 pass

        // 패스워드가 올바른지 확인
        if(password != passwordConfirm){
            AlertDialog.Builder(this).setMessage("비밀번호 확인에 문제가 있습니다. 다시 확인하여 입력해주시기 바랍니다.").show()
            binding.etPasswordConfirm.selectAll() //써있는 글씨를 모두 선택상태로 하여 손쉽게 새로 입력하기 쉽도록만듬
            return
        }

        var db:FirebaseFirestore= FirebaseFirestore.getInstance()

        // 이미 가입한 적이 있는 email 인지 검사
        // 필드값 중에 'email' 의 값이 EditText에 입력한 email과 같은 것이 있는 찾아달라고 요청
        db.collection("emailUsers")
            .whereEqualTo("email", email)
            .get().addOnSuccessListener {
                //같은 값을 가진 Document가 있다면.. 기존에 같은 email 이 있다는 것임
                if( it.documents.size > 0 ){
                    AlertDialog.Builder(this).setMessage("중복된 이메일이 있습니다. 다시 확인하여 입력해주시기 바랍니다.").show()
                    binding.etEmail.requestFocus() //selectAll() 하려면 포커스가 있어야 함.
                    binding.etEmail.selectAll()
                }else{
                    // 신규 email
                    // 저장할 데이터들을 하나로 묶기위해 HashMap
                    var user: MutableMap<String, String> = mutableMapOf()
                    user.put("email", email)
                    user.put("password", password)

                    // DB안에 Collection 명은 "emailUsers" 로 지정 [ RDBMS 의 테이블 이름 같은 역할 ]
                    // 별도의 Document 명을 주지 않으면 Random 값으로 설정됨. 이 랜덤값을 회원번호의 역할로 사용함.
                    db.collection("emailUsers").add(user).addOnSuccessListener {
                        AlertDialog.Builder(this)
                            .setMessage("축하합니다.\n회원가입이 완료되었습니다.")
                            .setPositiveButton("확인", object : DialogInterface.OnClickListener{
                                override fun onClick(p0: DialogInterface?, p1: Int) {
                                    finish()
                                }
                            }).show()
                    }.addOnFailureListener {
                        Toast.makeText(this, "회원가입 실패 : ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }


    }


}