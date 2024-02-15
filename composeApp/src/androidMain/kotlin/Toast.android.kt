import android.widget.Toast

actual fun showToast(context: MyContext, text: String) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}