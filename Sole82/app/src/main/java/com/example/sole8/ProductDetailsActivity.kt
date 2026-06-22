package com.example.sole8

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.sole8.models.api.FavoriteRequest
import com.example.sole8.models.api.ProductDetailsDto
import com.example.sole8.models.api.ProductSizeDto
import com.example.sole8.network.ApiClient
import com.example.sole8.util.FavoriteProductsCache
import com.google.android.material.button.MaterialButton
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class ProductDetailsActivity : AppCompatActivity() {

    private lateinit var name: TextView
    private lateinit var price: TextView
    private lateinit var image: ImageView
    private lateinit var favBtn: ImageView
    private lateinit var sizeContainer: LinearLayout
    private lateinit var desc: TextView

    private lateinit var button3D: ImageView
    private lateinit var productSceneView: SceneView
    private lateinit var modelProgressBar: ProgressBar

    private var selectedSize: ProductSizeDto? = null
    private var isFavorite = false
    private var currentProductId: Int = -1

    private lateinit var userPrefs: UserPreferences

    private var currentModelNode: ModelNode? = null
    private var is3DModeActive = false
    private var modelUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        userPrefs = UserPreferences(this)

        name = findViewById(R.id.detailsName)
        price = findViewById(R.id.detailsPrice)
        image = findViewById(R.id.detailsImage)
        favBtn = findViewById(R.id.favButton)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.detailsToolbar)
        toolbar.setNavigationOnClickListener { finish() }

        sizeContainer = findViewById(R.id.sizeContainer)
        desc = findViewById(R.id.detailsDescription)
        button3D = findViewById(R.id.button3D)
        productSceneView = findViewById(R.id.productSceneView)
        modelProgressBar = findViewById(R.id.modelProgressBar)

        val id = intent.getIntExtra("productId", -1)

        if (id == -1) {
            Toast.makeText(this, "Product not found", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        currentProductId = id
        loadProduct(id)

        favBtn.setOnClickListener { toggleFavorite() }

        val buyBtn = findViewById<MaterialButton>(R.id.buyBtn)
        buyBtn.setOnClickListener { addToCart() }
    }

    private fun loadProduct(id: Int) {
        lifecycleScope.launch {
            try {
                val product = ApiClient.productsApi.getProductDetails(id)
                setupProductUI(product)
                syncFavoriteStatus()
            } catch (e: Exception) {
                Toast.makeText(
                    this@ProductDetailsActivity,
                    "Ошибка загрузки товара",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupProductUI(product: ProductDetailsDto) {
        name.text = product.name
        price.text = "${product.price} ₽"
        desc.text = product.description

        Glide.with(this)
            .load(product.images.firstOrNull())
            .into(image)

        setupSizes(product.sizes)

        modelUrl = product.model3DUrl

        if (!modelUrl.isNullOrEmpty()) {
            button3D.visibility = View.VISIBLE
            button3D.setOnClickListener {
                if (is3DModeActive) {
                    disable3DMode()
                } else {
                    enable3DMode(modelUrl!!)
                }
            }
        } else {
            button3D.visibility = View.GONE
        }
    }

    private fun setupSizes(sizes: List<ProductSizeDto>) {
        sizeContainer.removeAllViews()
        val buttonsList = mutableListOf<Button>()

        for (sizeDto in sizes) {
            val btn = Button(this).apply {
                text = sizeDto.sizeValue.toString()
                textSize = 14f
                setBackgroundColor(Color.LTGRAY)
                setTextColor(Color.BLACK)

                // Если товара нет на складе, делаем кнопку недоступной
                if (sizeDto.stock <= 0) {
                    isEnabled = false
                    alpha = 0.5f
                }
            }

            btn.setOnClickListener {
                selectedSize = sizeDto
                for (b in buttonsList) {
                    b.setBackgroundColor(Color.LTGRAY)
                    b.setTextColor(Color.BLACK)
                }
                btn.setBackgroundColor(Color.BLACK)
                btn.setTextColor(Color.WHITE)
            }

            buttonsList.add(btn)
            sizeContainer.addView(btn)
        }
    }

    private fun enable3DMode(url: String) {
        is3DModeActive = true
        button3D.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
        image.visibility = View.GONE
        productSceneView.visibility = View.VISIBLE

        if (currentModelNode == null) {
            loadModelDirectly(url)
        } else {
            startModelRotation()
        }
    }

    private fun disable3DMode() {
        is3DModeActive = false
        button3D.setImageResource(R.drawable.ic_3d)
        productSceneView.visibility = View.GONE
        modelProgressBar.visibility = View.GONE
        image.visibility = View.VISIBLE
    }

    private fun loadModelDirectly(url: String) {
        modelProgressBar.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val localFile = withContext(Dispatchers.IO) {
                    val client = OkHttpClient()
                    val request = Request.Builder().url(url).build()
                    val response = client.newCall(request).execute()

                    if (!response.isSuccessful) {
                        throw Exception("Код ответа сервера: ${response.code}")
                    }

                    val body = response.body ?: throw Exception("Пустой ответ")
                    val tempFile = File.createTempFile("product_model_", ".glb", cacheDir)

                    tempFile.outputStream().use { output ->
                        body.byteStream().use { input ->
                            input.copyTo(output)
                        }
                    }
                    tempFile
                }

                val instance = productSceneView.modelLoader.createModelInstance(localFile)

                if (instance != null) {
                    currentModelNode = ModelNode(
                        modelInstance = instance,
                        autoAnimate = true,
                        scaleToUnits = 2.0f,
                        centerOrigin = Position(0.0f, 0.0f, 0.0f)
                    )

                    productSceneView.addChildNode(currentModelNode!!)
                    startModelRotation()
                } else {
                    Toast.makeText(
                        this@ProductDetailsActivity,
                        "Не удалось отобразить 3D-объект",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@ProductDetailsActivity,
                    "Ошибка загрузки: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
                disable3DMode()
            } finally {
                modelProgressBar.visibility = View.GONE
            }
        }
    }

    private fun startModelRotation() {
        val node = currentModelNode ?: return

        lifecycleScope.launch {
            while (is3DModeActive) {
                val currentRot = node.rotation
                node.rotation = Position(currentRot.x, currentRot.y + 1f, currentRot.z)
                delay(16)
            }
        }
    }

    private fun syncFavoriteStatus() {
        isFavorite = FavoriteProductsCache.isFavorite(currentProductId)
        updateFavIcon()
    }

    private fun updateFavIcon() {
        favBtn.setImageResource(
            if (isFavorite) R.drawable.ic_favorite
            else R.drawable.ic_favorite_border
        )
    }

    private fun toggleFavorite() {
        lifecycleScope.launch {
            try {
                if (isFavorite) {
                    ApiClient.favoritesApi.removeFromFavorites(FavoriteRequest(currentProductId))
                    FavoriteProductsCache.remove(currentProductId)
                    isFavorite = false
                    Toast.makeText(
                        this@ProductDetailsActivity,
                        "Удалено из избранного",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    ApiClient.favoritesApi.addToFavorites(FavoriteRequest(currentProductId))
                    FavoriteProductsCache.add(currentProductId)
                    isFavorite = true
                    Toast.makeText(
                        this@ProductDetailsActivity,
                        "Добавлено в избранное",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                updateFavIcon()
            } catch (e: Exception) {
                Toast.makeText(this@ProductDetailsActivity, "Ошибка: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun addToCart() {
        val size = selectedSize
        if (size == null) {
            Toast.makeText(this, "Выберите размер товара перед покупкой!", Toast.LENGTH_SHORT)
                .show()
            return
        }
        lifecycleScope.launch {
            try {
                ApiClient.cartApi.addToCart(
                    productId = currentProductId,
                    sizeId = size.id,
                    quantity = 1
                )
                Toast.makeText(this@ProductDetailsActivity, "Товар добавлен в корзину!", Toast.LENGTH_SHORT).show()
            }
            catch (e: Exception) {
                Toast.makeText(this@ProductDetailsActivity, "Ошибка: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}