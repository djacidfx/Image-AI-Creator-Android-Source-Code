package br.com.codapp.imagecreator.data.remote

import br.com.codapp.imagecreator.R

enum class ErrorType(val errorMsg: Int) {
    NONE(-1),
    IMAGE_POLICY(R.string.prompt_policy_error),
    TIMEOUT(R.string.timeout_error),
    GENERIC(R.string.generic_error)
}