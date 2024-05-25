import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Mascota(
    var id: String = "",
    var tipo: String = "",
    var nombre: String = "",
    var raza: String = "",
    var sexo: String = "",
    var edad: Int = 0,
    var direccion: String = "",
    var imageUrl: String = "",
    var estatus: String = "",
    var nota: String = ""
) : Parcelable