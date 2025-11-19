package com.miaumigo.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.widget.Switch;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.miaumigo.app.ProductDetailActivity;
import com.miaumigo.app.models.User;
import com.miaumigo.app.R;
import com.miaumigo.app.adapters.ProductAdapter;
import com.miaumigo.app.models.CartItem;
import com.miaumigo.app.models.Product;
import com.miaumigo.app.utils.CartManager;
import com.miaumigo.app.utils.GridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ProductsFragment extends Fragment implements ProductAdapter.OnProductActionListener {

    private static final long SEARCH_DELAY_MS = 300L;
    private static final int PAGE_SIZE = 12;

    private RecyclerView recyclerViewProducts;
    private MaterialAutoCompleteTextView editTextSearch;
    private LinearLayout textViewEmpty;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Switch switchInStock;
    private TextView textViewActiveFilters;
    private ChipGroup chipGroupQuickFilters;
    private MaterialButton buttonFilters;
    private MaterialButton buttonSort;
    
    private ProductAdapter productAdapter;
    private final List<Product> productList = new ArrayList<>();
    private final List<Product> filteredProductList = new ArrayList<>();
    private final List<Product> pagedProductList = new ArrayList<>();
    private final List<String> categoryOptions = new ArrayList<>();
    private final List<String> brandOptions = new ArrayList<>();
    private final List<String> vendorOptions = new ArrayList<>();

    private DatabaseReference productsReference;
    private ValueEventListener productsListener;
    private Handler searchHandler;
    private Runnable searchRunnable;
    private final Map<String, List<String>> synonymsMap = new HashMap<>();
    private ArrayAdapter<String> searchSuggestionsAdapter;
    private GridLayoutManager gridLayoutManager;
    private CartManager cartManager;
    private FirebaseUser currentUser;
    private String currentUserId;
    private boolean isVendor = false;

    private double minPriceRange = 0;
    private double maxPriceRange = 1000;
    private int currentPage = 0;
    private boolean isLoadingMore = false;

    private final ProductFilterState filterState = new ProductFilterState();
    private SortOption currentSortOption = SortOption.RELEVANCE;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        android.util.Log.d("ProductsFragment", "onCreateView iniciado");
        try {
            android.util.Log.d("ProductsFragment", "Tentando inflar layout...");
            View view = inflater.inflate(R.layout.fragment_products, container, false);
            android.util.Log.d("ProductsFragment", "Layout inflado com sucesso");
            
            if (view == null) {
                android.util.Log.e("ProductsFragment", "View é null após inflação");
                return createErrorView("Erro: view nula");
            }
            
            if (getContext() == null) {
                android.util.Log.e("ProductsFragment", "Context é null");
                return createErrorView("Erro: context nulo");
            }

            android.util.Log.d("ProductsFragment", "Inicializando views...");
            try {
                initViews(view);
                android.util.Log.d("ProductsFragment", "Views inicializadas");
            } catch (Exception e) {
                android.util.Log.e("ProductsFragment", "Erro crítico ao inicializar views", e);
                e.printStackTrace();
                // Continua mesmo se houver erro para tentar mostrar algo
            }
            
            // Inicializa handler primeiro
            android.util.Log.d("ProductsFragment", "Inicializando handler...");
            initSearchHandler();
            android.util.Log.d("ProductsFragment", "Handler inicializado");
            
            // Só configura o RecyclerView se todas as views foram inicializadas
            if (recyclerViewProducts != null && getContext() != null) {
                android.util.Log.d("ProductsFragment", "Configurando RecyclerView...");
                try {
                    setupRecyclerView();
                    android.util.Log.d("ProductsFragment", "RecyclerView configurado");
                } catch (Exception e) {
                    android.util.Log.e("ProductsFragment", "Erro ao configurar RecyclerView", e);
                    e.printStackTrace();
                }
            } else {
                android.util.Log.e("ProductsFragment", "recyclerViewProducts é null ou context é null!");
            }
            
            android.util.Log.d("ProductsFragment", "Configurando search...");
            try {
                if (getContext() != null && getView() != null) {
                    setupSearch();
                    android.util.Log.d("ProductsFragment", "Search configurado");
                }
            } catch (Exception e) {
                android.util.Log.e("ProductsFragment", "Erro ao configurar search", e);
                e.printStackTrace();
            }
            
            android.util.Log.d("ProductsFragment", "Configurando actions...");
            try {
                if (getContext() != null && getView() != null) {
                    setupActions();
                    android.util.Log.d("ProductsFragment", "Actions configuradas");
                }
            } catch (Exception e) {
                android.util.Log.e("ProductsFragment", "Erro ao configurar actions", e);
                e.printStackTrace();
            }
            
            // Inicializa synonyms
            android.util.Log.d("ProductsFragment", "Inicializando synonyms...");
            try {
                initSynonymsMap();
                android.util.Log.d("ProductsFragment", "Synonyms inicializados");
            } catch (Exception e) {
                android.util.Log.e("ProductsFragment", "Erro ao inicializar synonyms", e);
                e.printStackTrace();
            }
            
            // Verifica tipo de usuário de forma assíncrona
            android.util.Log.d("ProductsFragment", "Verificando tipo de usuário...");
            view.post(() -> {
                try {
                    if (getContext() != null && getView() != null) {
                        checkUserRole();
                        android.util.Log.d("ProductsFragment", "Tipo de usuário verificado");
                    }
                } catch (Exception e) {
                    android.util.Log.e("ProductsFragment", "Erro ao verificar tipo de usuário", e);
                    e.printStackTrace();
                }
            });
            
            // Carrega produtos de forma assíncrona para não travar a UI
            android.util.Log.d("ProductsFragment", "Postando carregamento de produtos...");
            view.postDelayed(() -> {
                try {
                    if (getContext() != null && getView() != null) {
                        android.util.Log.d("ProductsFragment", "Iniciando carregamento de produtos...");
                        loadProducts();
                    } else {
                        android.util.Log.w("ProductsFragment", "Context ou View é null ao tentar carregar produtos");
                    }
                } catch (Exception e) {
                    android.util.Log.e("ProductsFragment", "Erro ao carregar produtos", e);
                    e.printStackTrace();
                    if (getContext() != null && getView() != null) {
                        showLoading(false);
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }
            }, 100);

            android.util.Log.d("ProductsFragment", "onCreateView concluído com sucesso");
            return view;
        } catch (Exception e) {
            android.util.Log.e("ProductsFragment", "Erro crítico em onCreateView", e);
            e.printStackTrace();
            return createErrorView("Erro: " + e.getMessage());
        }
    }
    
    private View createErrorView(String message) {
        if (getContext() == null) {
            return null;
        }
        android.widget.TextView errorView = new android.widget.TextView(getContext());
        errorView.setText(message);
        errorView.setPadding(32, 32, 32, 32);
        errorView.setTextColor(android.graphics.Color.RED);
        return errorView;
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
            searchRunnable = null;
        }
        if (productsReference != null && productsListener != null) {
            productsReference.removeEventListener(productsListener);
            productsListener = null;
        }
        if (recyclerViewProducts != null) {
            recyclerViewProducts.clearOnScrollListeners();
        }
    }

    private void initViews(View view) {
        android.util.Log.d("ProductsFragment", "initViews iniciado");
        if (view == null) {
            android.util.Log.e("ProductsFragment", "View é null em initViews");
            throw new IllegalArgumentException("View não pode ser null");
        }
        
        if (getContext() == null) {
            android.util.Log.e("ProductsFragment", "Context é null em initViews");
            throw new IllegalStateException("Context não pode ser null");
        }
        
        try {
            
            android.util.Log.d("ProductsFragment", "Buscando recyclerViewProducts...");
            recyclerViewProducts = view.findViewById(R.id.recyclerViewProducts);
            android.util.Log.d("ProductsFragment", "recyclerViewProducts: " + (recyclerViewProducts != null ? "encontrado" : "NULL"));
            
            android.util.Log.d("ProductsFragment", "Buscando editTextSearch...");
            editTextSearch = view.findViewById(R.id.editTextSearch);
            android.util.Log.d("ProductsFragment", "editTextSearch: " + (editTextSearch != null ? "encontrado" : "NULL"));
            
            android.util.Log.d("ProductsFragment", "Buscando textViewEmpty (LinearLayout)...");
            textViewEmpty = view.findViewById(R.id.textViewEmpty);
            android.util.Log.d("ProductsFragment", "textViewEmpty: " + (textViewEmpty != null ? "encontrado" : "NULL"));
            
            android.util.Log.d("ProductsFragment", "Buscando progressBar...");
            progressBar = view.findViewById(R.id.progressBar);
            android.util.Log.d("ProductsFragment", "progressBar: " + (progressBar != null ? "encontrado" : "NULL"));
            
            android.util.Log.d("ProductsFragment", "Buscando swipeRefreshLayout...");
            swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
            android.util.Log.d("ProductsFragment", "swipeRefreshLayout: " + (swipeRefreshLayout != null ? "encontrado" : "NULL"));
            
            android.util.Log.d("ProductsFragment", "Buscando switchInStock...");
            switchInStock = view.findViewById(R.id.switchInStock);
            android.util.Log.d("ProductsFragment", "switchInStock: " + (switchInStock != null ? "encontrado" : "NULL"));
            
            android.util.Log.d("ProductsFragment", "Buscando textViewActiveFilters...");
            textViewActiveFilters = view.findViewById(R.id.textViewActiveFilters);
            android.util.Log.d("ProductsFragment", "textViewActiveFilters: " + (textViewActiveFilters != null ? "encontrado" : "NULL"));
            
            android.util.Log.d("ProductsFragment", "Buscando chipGroupQuickFilters...");
            chipGroupQuickFilters = view.findViewById(R.id.chipGroupQuickFilters);
            android.util.Log.d("ProductsFragment", "chipGroupQuickFilters: " + (chipGroupQuickFilters != null ? "encontrado" : "NULL"));
            
            android.util.Log.d("ProductsFragment", "Buscando buttonFilters...");
            buttonFilters = view.findViewById(R.id.buttonFilters);
            android.util.Log.d("ProductsFragment", "buttonFilters: " + (buttonFilters != null ? "encontrado" : "NULL"));
            
            android.util.Log.d("ProductsFragment", "Buscando buttonSort...");
            buttonSort = view.findViewById(R.id.buttonSort);
            android.util.Log.d("ProductsFragment", "buttonSort: " + (buttonSort != null ? "encontrado" : "NULL"));
            
            // Verifica se as views essenciais foram encontradas
            if (recyclerViewProducts == null) {
                android.util.Log.e("ProductsFragment", "ERRO: recyclerViewProducts não encontrado!");
                return;
            }
            
            android.util.Log.d("ProductsFragment", "Inicializando CartManager e adapters...");
            try {
                cartManager = CartManager.getInstance(getContext());
                android.util.Log.d("ProductsFragment", "CartManager inicializado");
                
                searchSuggestionsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
                android.util.Log.d("ProductsFragment", "searchSuggestionsAdapter criado");
                
                if (editTextSearch != null) {
                    editTextSearch.setAdapter(searchSuggestionsAdapter);
                    android.util.Log.d("ProductsFragment", "Adapter atribuído ao editTextSearch");
                } else {
                    android.util.Log.w("ProductsFragment", "editTextSearch é null, não foi possível atribuir adapter");
                }
            } catch (Exception e) {
                android.util.Log.e("ProductsFragment", "Erro ao inicializar adapters", e);
                e.printStackTrace();
            }
            
            android.util.Log.d("ProductsFragment", "initViews concluído com sucesso");
        } catch (Exception e) {
            android.util.Log.e("ProductsFragment", "Erro crítico em initViews", e);
            e.printStackTrace();
            throw e; // Re-lança para ser capturado no onCreateView
        }
    }
    
    private void checkUserRole() {
        if (getContext() == null || getView() == null) {
            return;
        }
        try {
            currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                currentUserId = currentUser.getUid();
                DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users").child(currentUserId);
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (getContext() == null || getView() == null) {
                            return;
                        }
                        try {
                            if (snapshot.exists()) {
                                User user = snapshot.getValue(User.class);
                                if (user != null) {
                                    isVendor = "vendor".equals(user.getRole());
                                    android.util.Log.d("ProductsFragment", "Usuário é vendedor: " + isVendor);
                                }
                            }
                        } catch (Exception e) {
                            android.util.Log.e("ProductsFragment", "Erro ao verificar role do usuário", e);
                            isVendor = false;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        android.util.Log.e("ProductsFragment", "Erro ao verificar role: " + error.getMessage());
                        isVendor = false;
                    }
                });
            } else {
                isVendor = false;
            }
        } catch (Exception e) {
            android.util.Log.e("ProductsFragment", "Erro crítico ao verificar role", e);
            isVendor = false;
        }
    }
    
    private void initSearchHandler() {
        if (getContext() == null) {
            return;
        }
        try {
            searchHandler = new Handler(Looper.getMainLooper());
        } catch (Exception e) {
            android.util.Log.e("ProductsFragment", "Erro ao criar Handler", e);
        }
    }
    
    private void initSynonymsMap() {
        // Usando Arrays.asList() para compatibilidade com Android mais antigo
        synonymsMap.put("cachorro", Arrays.asList("cão", "cães", "dog", "canino"));
        synonymsMap.put("gato", Arrays.asList("felino", "cat", "gatinho"));
        synonymsMap.put("comida", Arrays.asList("ração", "alimento", "feed"));
        synonymsMap.put("brinquedo", Arrays.asList("toy", "brincadeira", "diversão"));
        synonymsMap.put("remedio", Arrays.asList("remédio", "medicamento", "medicina"));
        synonymsMap.put("higiene", Arrays.asList("banho", "limpeza", "shampoo"));
        synonymsMap.put("caminha", Arrays.asList("cama", "colchão", "almofada"));
        synonymsMap.put("camisa", Arrays.asList("camiseta", "blusa", "roupa"));
        synonymsMap.put("passear", Arrays.asList("passeio", "coleira", "guia"));
    }

    private void setupRecyclerView() {
        if (recyclerViewProducts == null || getContext() == null) {
            android.util.Log.e("ProductsFragment", "setupRecyclerView: recyclerViewProducts ou context é null");
            return;
        }
        try {
            // Garante que as listas existem
            if (pagedProductList == null) {
                android.util.Log.e("ProductsFragment", "pagedProductList é null!");
                return;
            }
            
            // Garante que o adapter existe antes de configurar
            if (productAdapter == null) {
                try {
                    productAdapter = new ProductAdapter(pagedProductList, this, false);
                    android.util.Log.d("ProductsFragment", "ProductAdapter criado");
                } catch (Exception e) {
                    android.util.Log.e("ProductsFragment", "Erro ao criar ProductAdapter", e);
                    e.printStackTrace();
                    return;
                }
            }
            
            int spanCount = calculateSpanCount();
            gridLayoutManager = new GridLayoutManager(getContext(), spanCount);
            recyclerViewProducts.setLayoutManager(gridLayoutManager);
            
            // Tenta adicionar o decorator com espaçamento
            try {
                int spacing = getResources().getDimensionPixelSize(R.dimen.product_grid_spacing);
                recyclerViewProducts.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
            } catch (Exception e) {
                // Se a dimensão não existir ou houver erro, usar valor padrão
                int spacing = (int) (8 * getResources().getDisplayMetrics().density);
                recyclerViewProducts.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
            }
            
            recyclerViewProducts.setAdapter(productAdapter);
            android.util.Log.d("ProductsFragment", "Adapter configurado no RecyclerView");
            recyclerViewProducts.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (dy <= 0 || isLoadingMore || getContext() == null || getView() == null) {
                        return;
                    }
                    try {
                        if (!recyclerView.canScrollVertically(1)) {
                            isLoadingMore = true;
                            loadNextPage();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        isLoadingMore = false;
                    }
                }
            });
        } catch (Exception e) {
            android.util.Log.e("ProductsFragment", "Erro em setupRecyclerView", e);
            e.printStackTrace();
            // Em caso de erro, pelo menos configura o adapter básico
            if (productAdapter == null && getContext() != null) {
                productAdapter = new ProductAdapter(pagedProductList, this);
            }
            if (recyclerViewProducts != null && getContext() != null) {
                recyclerViewProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
                recyclerViewProducts.setAdapter(productAdapter);
            }
        }
    }

    private int calculateSpanCount() {
        if (getContext() == null) {
            return 2; // Default
        }
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float screenWidthDp = metrics.widthPixels / metrics.density;
        // Calculate based on card width (including margins)
        // Card width should be around 160-180dp for comfortable viewing
        float cardWidthDp = 160f; // Ideal card width
        int span = (int) Math.floor(screenWidthDp / cardWidthDp);
        // Ensure minimum 2 columns, maximum 4 columns
        return Math.max(2, Math.min(span, 4));
    }

    private void setupSearch() {
        if (editTextSearch == null || searchHandler == null) {
            return;
        }
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (getContext() == null || getView() == null) {
                    return;
                }
                if (searchRunnable != null && searchHandler != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> {
                    if (getContext() != null && getView() != null) {
                        applyFiltersAndSort();
                    }
                };
                if (searchHandler != null) {
                searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupActions() {
        if (buttonFilters != null) {
            buttonFilters.setOnClickListener(v -> showFiltersBottomSheet());
        }
        if (buttonSort != null) {
            buttonSort.setOnClickListener(v -> showSortDialog());
        }
        if (switchInStock != null) {
            switchInStock.setOnCheckedChangeListener((buttonView, isChecked) -> {
                filterState.inStockQuickToggle = isChecked;
                applyFiltersAndSort();
            });
        }
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::reloadProducts);
        }
    }

    private void reloadProducts() {
        if (productsReference != null && productsListener != null) {
            productsReference.removeEventListener(productsListener);
        }
        loadProducts();
    }

    private void loadProducts() {
        if (getContext() == null || getView() == null) {
            android.util.Log.w("ProductsFragment", "loadProducts: Context ou View é null, abortando");
            return;
        }
        
        try {
            showLoading(true);
            
            if (productsReference == null) {
                try {
                    productsReference = FirebaseDatabase.getInstance().getReference("products");
                    android.util.Log.d("ProductsFragment", "productsReference criado");
                } catch (Exception e) {
                    android.util.Log.e("ProductsFragment", "Erro ao criar productsReference", e);
                    showLoading(false);
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    Toast.makeText(getContext(), "Erro ao conectar com o banco de dados", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            // Remove listener anterior se existir
            if (productsListener != null) {
                try {
                    productsReference.removeEventListener(productsListener);
                    android.util.Log.d("ProductsFragment", "Listener anterior removido");
                } catch (Exception e) {
                    android.util.Log.e("ProductsFragment", "Erro ao remover listener anterior", e);
                }
                productsListener = null;
            }

            productsListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (getContext() == null || getView() == null) {
                        android.util.Log.w("ProductsFragment", "onDataChange: Context ou View é null");
                        return;
                    }

                    try {
                        productList.clear();

                        for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                            try {
                                Product product = productSnapshot.getValue(Product.class);
                                if (product != null) {
                                    product.setId(productSnapshot.getKey());
                                    
                                    // Filtra produtos visíveis para clientes (se não for vendedor)
                                    if (!isVendor) {
                                        if (product.isVisibleToCustomers() && product.isInStock()) {
                                            productList.add(product);
                                        }
                                    } else {
                                        // Vendedores veem todos os produtos
                                        productList.add(product);
                                    }
                                }
                            } catch (Exception e) {
                                android.util.Log.e("ProductsFragment", "Erro ao processar produto: " + productSnapshot.getKey(), e);
                                // Continua processando outros produtos
                            }
                        }

                        android.util.Log.d("ProductsFragment", "Produtos carregados: " + productList.size());

                        // Atualiza opções de filtro de forma segura
                        try {
                            updatePriceRange();
                        } catch (Exception e) {
                            android.util.Log.e("ProductsFragment", "Erro ao atualizar price range", e);
                        }
                        
                        try {
                            updateFacetOptions();
                        } catch (Exception e) {
                            android.util.Log.e("ProductsFragment", "Erro ao atualizar facet options", e);
                        }
                        
                        try {
                            updateSearchSuggestions();
                        } catch (Exception e) {
                            android.util.Log.e("ProductsFragment", "Erro ao atualizar search suggestions", e);
                        }
                        
                        try {
                            renderQuickFilters();
                        } catch (Exception e) {
                            android.util.Log.e("ProductsFragment", "Erro ao renderizar quick filters", e);
                        }

                        // Aplica filtros e ordenação de forma segura
                        try {
                            resetPagination();
                            applyFiltersAndSort();
                        } catch (Exception e) {
                            android.util.Log.e("ProductsFragment", "Erro ao aplicar filtros e ordenação", e);
                            e.printStackTrace();
                            // Garante que os produtos sejam exibidos mesmo se houver erro
                            if (productAdapter != null && pagedProductList != null && productList != null) {
                                try {
                                    pagedProductList.clear();
                                    pagedProductList.addAll(productList);
                                    if (productAdapter != null) {
                                        productAdapter.notifyDataSetChanged();
                                    }
                                    updateEmptyState();
                                } catch (Exception ex) {
                                    android.util.Log.e("ProductsFragment", "Erro ao atualizar adapter no fallback", ex);
                                }
                            }
                        }

                        showLoading(false);
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("ProductsFragment", "Erro ao processar dados do Firebase", e);
                        e.printStackTrace();
                        showLoading(false);
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Erro ao carregar produtos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    android.util.Log.e("ProductsFragment", "Erro Firebase: " + error.getMessage(), error.toException());
                    if (getContext() == null || getView() == null) {
                        return;
                    }
                    showLoading(false);
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    Toast.makeText(getContext(), "Erro ao carregar produtos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            };

            productsReference.addValueEventListener(productsListener);
            android.util.Log.d("ProductsFragment", "Listener adicionado ao productsReference");
            
        } catch (Exception e) {
            android.util.Log.e("ProductsFragment", "Erro crítico em loadProducts", e);
            e.printStackTrace();
            showLoading(false);
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            if (getContext() != null) {
                Toast.makeText(getContext(), "Erro ao carregar produtos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updatePriceRange() {
        double min = Double.MAX_VALUE;
        double max = 0;
        for (Product product : productList) {
            double price = product.getPrice();
            min = Math.min(min, price);
            max = Math.max(max, price);
        }
        if (min == Double.MAX_VALUE) {
            min = 0;
        }
        minPriceRange = Math.floor(min);
        maxPriceRange = Math.ceil(max <= 0 ? 1000 : max);
        filterState.ensurePriceInitialized(minPriceRange, maxPriceRange);
    }

    private void updateFacetOptions() {
        Set<String> categorySet = new LinkedHashSet<>();
        Set<String> brandSet = new LinkedHashSet<>();
        Set<String> vendorSet = new LinkedHashSet<>();

        for (Product product : productList) {
            if (product.getCategory() != null && !product.getCategory().isEmpty()) {
                categorySet.add(product.getCategory());
            }
            if (product.getBrand() != null && !product.getBrand().isEmpty()) {
                brandSet.add(product.getBrand());
            }
            if (product.getVendorName() != null && !product.getVendorName().isEmpty()) {
                vendorSet.add(product.getVendorName());
            }
        }

        categoryOptions.clear();
        categoryOptions.addAll(categorySet);
        brandOptions.clear();
        brandOptions.addAll(brandSet);
        vendorOptions.clear();
        vendorOptions.addAll(vendorSet);

        if (filterState.category != null && !categorySet.contains(filterState.category)) {
            filterState.category = null;
        }
        if (filterState.brand != null && !brandSet.contains(filterState.brand)) {
            filterState.brand = null;
        }
        if (filterState.vendor != null && !vendorSet.contains(filterState.vendor)) {
            filterState.vendor = null;
        }
    }

    private void updateSearchSuggestions() {
        if (searchSuggestionsAdapter == null || getContext() == null) {
            return;
        }
        // Limitar sugestões para não travar a UI
        List<String> suggestions = new ArrayList<>();
        int maxSuggestions = Math.min(50, productList.size());
        for (int i = 0; i < maxSuggestions; i++) {
            Product product = productList.get(i);
            if (product != null && product.getName() != null && !product.getName().isEmpty()) {
                suggestions.add(product.getName());
            }
        }
        searchSuggestionsAdapter.clear();
        searchSuggestionsAdapter.addAll(suggestions);
        searchSuggestionsAdapter.notifyDataSetChanged();
    }

    private void renderQuickFilters() {
        if (chipGroupQuickFilters == null || getContext() == null) {
            return;
        }
        chipGroupQuickFilters.removeAllViews();
        int max = Math.min(6, categoryOptions.size());
        for (int i = 0; i < max; i++) {
            String category = categoryOptions.get(i);
            Chip chip = new Chip(getContext());
            chip.setText(category);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.surface_variant);
            chip.setChipStrokeColorResource(R.color.border_light);
            float strokeWidth = getResources().getDisplayMetrics().density * 1.5f;
            chip.setChipStrokeWidth(strokeWidth);
            chip.setChecked(category.equalsIgnoreCase(filterState.category));
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    filterState.category = category;
                    uncheckOtherChips(chip);
                } else if (category.equalsIgnoreCase(filterState.category)) {
                    filterState.category = null;
                }
                applyFiltersAndSort();
            });
            chipGroupQuickFilters.addView(chip);
        }
    }

    private void uncheckOtherChips(Chip selectedChip) {
        for (int i = 0; i < chipGroupQuickFilters.getChildCount(); i++) {
            View child = chipGroupQuickFilters.getChildAt(i);
            if (child instanceof Chip && child != selectedChip) {
                ((Chip) child).setChecked(false);
            }
        }
    }

    private void applyFiltersAndSort() {
        if (getContext() == null || getView() == null || productList == null) {
            android.util.Log.w("ProductsFragment", "applyFiltersAndSort: Context, View ou productList é null");
            return;
        }
        
        try {
            if (filteredProductList == null || pagedProductList == null || productAdapter == null) {
                android.util.Log.w("ProductsFragment", "applyFiltersAndSort: Listas ou adapter são null");
                return;
            }
            
            filterProducts();
            sortProducts();
            resetPagination();
            
            // Atualiza UI na thread principal
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    try {
                        if (productAdapter != null) {
                            productAdapter.notifyDataSetChanged();
                        }
                        updateEmptyState();
                        updateActiveFiltersLabel();
                    } catch (Exception e) {
                        android.util.Log.e("ProductsFragment", "Erro ao atualizar UI", e);
                    }
                });
            } else {
                if (productAdapter != null) {
                    productAdapter.notifyDataSetChanged();
                }
                updateEmptyState();
                updateActiveFiltersLabel();
            }
        } catch (Exception e) {
            android.util.Log.e("ProductsFragment", "Erro em applyFiltersAndSort", e);
            e.printStackTrace();
            // Em caso de erro, pelo menos atualiza o estado vazio
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    try {
                        if (productAdapter != null) {
                            productAdapter.notifyDataSetChanged();
                        }
                        updateEmptyState();
                    } catch (Exception ex) {
                        android.util.Log.e("ProductsFragment", "Erro ao atualizar UI no catch", ex);
                    }
                });
            }
        }
    }

    private void filterProducts() {
        if (filteredProductList == null || productList == null) {
            android.util.Log.w("ProductsFragment", "filterProducts: Listas são null");
            return;
        }
        
        try {
            filteredProductList.clear();
            String query = "";
            if (editTextSearch != null && editTextSearch.getText() != null) {
                query = editTextSearch.getText().toString().trim().toLowerCase(Locale.ROOT);
            }
            List<String> searchTerms = buildSearchTerms(query);

            for (Product product : productList) {
                if (product == null) {
                    continue;
                }
                try {
                    if (!matchesSearch(product, searchTerms)) {
                        continue;
                    }
                    if (!matchesFilters(product)) {
                        continue;
                    }
                    filteredProductList.add(product);
                } catch (Exception e) {
                    android.util.Log.e("ProductsFragment", "Erro ao filtrar produto: " + product.getId(), e);
                    // Continua processando outros produtos
                }
            }
        } catch (Exception e) {
            android.util.Log.e("ProductsFragment", "Erro crítico em filterProducts", e);
            e.printStackTrace();
        }
    }

    private List<String> buildSearchTerms(String query) {
        List<String> searchTerms = new ArrayList<>();
        if (query.isEmpty()) {
            return searchTerms;
        }
        searchTerms.add(query);
            for (Map.Entry<String, List<String>> entry : synonymsMap.entrySet()) {
            if (query.contains(entry.getKey())) {
                    searchTerms.addAll(entry.getValue());
                }
                for (String synonym : entry.getValue()) {
                if (query.contains(synonym)) {
                        searchTerms.add(entry.getKey());
                        searchTerms.addAll(entry.getValue());
                        break;
                    }
                }
        }
        return searchTerms;
    }

    private boolean matchesSearch(Product product, List<String> searchTerms) {
        if (searchTerms.isEmpty()) {
            return true;
        }
        String productName = product.getName() != null ? product.getName().toLowerCase(Locale.ROOT) : "";
        String productDesc = product.getDescription() != null ? product.getDescription().toLowerCase(Locale.ROOT) : "";
                for (String term : searchTerms) {
                    if (productName.contains(term) || productDesc.contains(term)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesFilters(Product product) {
        double price = product.getPrice();
        if (price < filterState.minPrice || price > filterState.maxPrice) {
            return false;
        }
        if (filterState.category != null && product.getCategory() != null &&
                !product.getCategory().equalsIgnoreCase(filterState.category)) {
            return false;
        }
        if (filterState.brand != null && product.getBrand() != null &&
                !product.getBrand().equalsIgnoreCase(filterState.brand)) {
            return false;
        }
        if (filterState.vendor != null && product.getVendorName() != null &&
                !product.getVendorName().equalsIgnoreCase(filterState.vendor)) {
            return false;
        }
        if (product.getRating() < filterState.minRating) {
            return false;
        }
        return matchesAvailability(product);
    }

    private boolean matchesAvailability(Product product) {
        if (filterState.inStockQuickToggle) {
            return product.isInStock();
        }
        boolean inStockChip = filterState.availabilityInStock;
        boolean outOfStockChip = filterState.availabilityOutOfStock;
        if (!inStockChip && !outOfStockChip) {
            return true;
        }
        if (product.isInStock()) {
            return inStockChip;
        } else {
            return outOfStockChip;
        }
    }

    private void sortProducts() {
        Comparator<Product> comparator;
        switch (currentSortOption) {
            case BEST_SELLERS:
                comparator = (p1, p2) -> Long.compare(p2.getSalesCount(), p1.getSalesCount());
                break;
            case PRICE_LOW_HIGH:
                comparator = Comparator.comparingDouble(Product::getPrice);
                break;
            case PRICE_HIGH_LOW:
                comparator = (p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice());
                break;
            case NEWEST:
                comparator = (p1, p2) -> Long.compare(
                        Math.max(p2.getUpdatedAt(), p2.getCreatedAt()),
                        Math.max(p1.getUpdatedAt(), p1.getCreatedAt()));
                        break;
            case RELEVANCE:
            default:
                comparator = (p1, p2) -> {
                    int ratingCompare = Double.compare(p2.getRating(), p1.getRating());
                    if (ratingCompare != 0) {
                        return ratingCompare;
                    }
                    return Long.compare(p2.getSalesCount(), p1.getSalesCount());
                };
                break;
        }
        Collections.sort(filteredProductList, comparator);
    }

    private void resetPagination() {
        if (pagedProductList == null || filteredProductList == null) {
            android.util.Log.w("ProductsFragment", "resetPagination: Listas são null");
            return;
        }
        
        try {
            pagedProductList.clear();
            currentPage = 0;
            loadNextPage();
        } catch (Exception e) {
            android.util.Log.e("ProductsFragment", "Erro em resetPagination", e);
            e.printStackTrace();
        }
    }

    private void loadNextPage() {
        if (productAdapter == null || pagedProductList == null || filteredProductList == null) {
            isLoadingMore = false;
            return;
        }
        
        try {
            if (filteredProductList.isEmpty()) {
                productAdapter.notifyDataSetChanged();
                isLoadingMore = false;
                return;
            }
            
            int start = currentPage * PAGE_SIZE;
            if (start >= filteredProductList.size()) {
                isLoadingMore = false;
                return;
            }
            
            int end = Math.min(start + PAGE_SIZE, filteredProductList.size());
            pagedProductList.addAll(filteredProductList.subList(start, end));
            currentPage++;
            
            if (productAdapter != null) {
                productAdapter.notifyDataSetChanged();
            }
            isLoadingMore = false;
        } catch (Exception e) {
            android.util.Log.e("ProductsFragment", "Erro em loadNextPage", e);
            e.printStackTrace();
            isLoadingMore = false;
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void updateEmptyState() {
        if (textViewEmpty == null || recyclerViewProducts == null) {
            return;
        }
        boolean isEmpty = pagedProductList.isEmpty();
        textViewEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerViewProducts.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void updateActiveFiltersLabel() {
        if (textViewActiveFilters == null || getContext() == null) {
            return;
        }
        int activeFilters = 0;
        if (filterState.category != null) activeFilters++;
        if (filterState.brand != null) activeFilters++;
        if (filterState.vendor != null) activeFilters++;
        if (filterState.minRating > 0) activeFilters++;
        if (filterState.minPrice > minPriceRange || filterState.maxPrice < maxPriceRange) activeFilters++;
        if (filterState.inStockQuickToggle) activeFilters++;
        if (filterState.availabilityInStock || filterState.availabilityOutOfStock) activeFilters++;

        if (activeFilters == 0) {
            textViewActiveFilters.setText(R.string.no_filters_applied);
        } else {
            textViewActiveFilters.setText(getString(R.string.active_filters_count, activeFilters));
        }
    }

    private void showFiltersBottomSheet() {
        if (getContext() == null || getView() == null) {
            android.util.Log.e("ProductsFragment", "Context ou View é null ao abrir filtros");
            return;
        }
        try {
            android.util.Log.d("ProductsFragment", "Abrindo bottom sheet de filtros...");
            BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
            View sheetView = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_product_filters, null, false);
            
            if (sheetView == null) {
                android.util.Log.e("ProductsFragment", "Falha ao inflar layout do bottom sheet");
                Toast.makeText(getContext(), "Erro ao carregar filtros", Toast.LENGTH_SHORT).show();
                return;
            }
            
            dialog.setContentView(sheetView);

            RangeSlider sliderPrice = sheetView.findViewById(R.id.sliderPrice);
            TextView textViewPriceRange = sheetView.findViewById(R.id.textViewPriceRange);
            MaterialAutoCompleteTextView autoCategory = sheetView.findViewById(R.id.autoCompleteCategory);
            MaterialAutoCompleteTextView autoBrand = sheetView.findViewById(R.id.autoCompleteBrand);
            MaterialAutoCompleteTextView autoVendor = sheetView.findViewById(R.id.autoCompleteVendor);
            ChipGroup chipGroupRating = sheetView.findViewById(R.id.chipGroupRating);
            Chip chipRating4 = sheetView.findViewById(R.id.chipRating4);
            Chip chipRating3 = sheetView.findViewById(R.id.chipRating3);
            Chip chipRating2 = sheetView.findViewById(R.id.chipRating2);
            Chip chipRatingAll = sheetView.findViewById(R.id.chipRatingAll);
            Chip chipAvailabilityInStock = sheetView.findViewById(R.id.chipAvailabilityInStock);
            Chip chipAvailabilityOutOfStock = sheetView.findViewById(R.id.chipAvailabilityOutOfStock);
            MaterialButton buttonApply = sheetView.findViewById(R.id.buttonApplyFilters);
            MaterialButton buttonClear = sheetView.findViewById(R.id.buttonClearFilters);

            // Verificar se todas as views essenciais foram encontradas
            if (sliderPrice == null || textViewPriceRange == null || buttonApply == null || buttonClear == null) {
                android.util.Log.e("ProductsFragment", "Views essenciais do bottom sheet não encontradas");
                Toast.makeText(getContext(), "Erro ao carregar filtros", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                sliderPrice.setValueFrom((float) minPriceRange);
                sliderPrice.setValueTo((float) maxPriceRange);
                sliderPrice.setValues((float) filterState.minPrice, (float) filterState.maxPrice);
                textViewPriceRange.setText(formatPriceRange(filterState.minPrice, filterState.maxPrice));
                sliderPrice.addOnChangeListener((slider, value, fromUser) -> {
                    try {
                        List<Float> values = slider.getValues();
                        if (values != null && values.size() >= 2 && textViewPriceRange != null) {
                            textViewPriceRange.setText(formatPriceRange(values.get(0), values.get(1)));
                        }
                    } catch (Exception e) {
                        android.util.Log.e("ProductsFragment", "Erro ao atualizar range de preço", e);
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("ProductsFragment", "Erro ao configurar slider de preço", e);
            }

            try {
                if (autoCategory != null && categoryOptions != null && !categoryOptions.isEmpty()) {
                    autoCategory.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categoryOptions));
                    if (filterState.category != null) {
                        autoCategory.setText(filterState.category, false);
                    }
                }
                
                if (autoBrand != null && brandOptions != null && !brandOptions.isEmpty()) {
                    autoBrand.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, brandOptions));
                    if (filterState.brand != null) {
                        autoBrand.setText(filterState.brand, false);
                    }
                }
                
                if (autoVendor != null && vendorOptions != null && !vendorOptions.isEmpty()) {
                    autoVendor.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, vendorOptions));
                    if (filterState.vendor != null) {
                        autoVendor.setText(filterState.vendor, false);
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("ProductsFragment", "Erro ao configurar autocomplete", e);
            }

            try {
                if (chipGroupRating != null && chipRatingAll != null && chipRating2 != null && 
                    chipRating3 != null && chipRating4 != null) {
                    selectRatingChip(chipGroupRating, chipRatingAll, chipRating2, chipRating3, chipRating4, filterState.minRating);
                }
                
                if (chipAvailabilityInStock != null) {
                    chipAvailabilityInStock.setChecked(filterState.availabilityInStock);
                }
                
                if (chipAvailabilityOutOfStock != null) {
                    chipAvailabilityOutOfStock.setChecked(filterState.availabilityOutOfStock);
                }
            } catch (Exception e) {
                android.util.Log.e("ProductsFragment", "Erro ao configurar chips", e);
            }

            buttonApply.setOnClickListener(v -> {
                try {
                    List<Float> values = sliderPrice.getValues();
                    if (values != null && values.size() >= 2) {
                        filterState.minPrice = values.get(0);
                        filterState.maxPrice = values.get(1);
                    }
                    filterState.category = autoCategory != null ? getOrNull(autoCategory) : null;
                    filterState.brand = autoBrand != null ? getOrNull(autoBrand) : null;
                    filterState.vendor = autoVendor != null ? getOrNull(autoVendor) : null;
                    
                    if (chipGroupRating != null && chipRatingAll != null && chipRating4 != null && 
                        chipRating3 != null && chipRating2 != null) {
                        filterState.minRating = resolveSelectedRating(chipGroupRating, chipRatingAll, chipRating4, chipRating3, chipRating2);
                    }
                    
                    if (chipAvailabilityInStock != null) {
                        filterState.availabilityInStock = chipAvailabilityInStock.isChecked();
                    }
                    
                    if (chipAvailabilityOutOfStock != null) {
                        filterState.availabilityOutOfStock = chipAvailabilityOutOfStock.isChecked();
                    }
                    
                    dialog.dismiss();
                    applyFiltersAndSort();
                } catch (Exception e) {
                    android.util.Log.e("ProductsFragment", "Erro ao aplicar filtros", e);
                    e.printStackTrace();
                }
            });

            buttonClear.setOnClickListener(v -> {
                try {
                    filterState.reset(minPriceRange, maxPriceRange);
                    if (switchInStock != null) {
                        switchInStock.setChecked(false);
                    }
                    if (chipGroupQuickFilters != null) {
                        chipGroupQuickFilters.clearCheck();
                    }
                    dialog.dismiss();
                    applyFiltersAndSort();
                } catch (Exception e) {
                    android.util.Log.e("ProductsFragment", "Erro ao limpar filtros", e);
                    e.printStackTrace();
                }
            });

            dialog.show();
            android.util.Log.d("ProductsFragment", "Bottom sheet de filtros aberto com sucesso");
        } catch (Exception e) {
            android.util.Log.e("ProductsFragment", "Erro crítico ao abrir filtros", e);
            e.printStackTrace();
            if (getContext() != null) {
                Toast.makeText(getContext(), "Erro ao abrir filtros: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void selectRatingChip(ChipGroup chipGroupRating, Chip chipAll, Chip chip2, Chip chip3, Chip chip4, float currentRating) {
        if (currentRating >= 4f) {
            chipGroupRating.check(chip4.getId());
        } else if (currentRating >= 3f) {
            chipGroupRating.check(chip3.getId());
        } else if (currentRating >= 2f) {
            chipGroupRating.check(chip2.getId());
        } else {
            chipGroupRating.check(chipAll.getId());
        }
    }

    private float resolveSelectedRating(ChipGroup chipGroupRating, Chip chipAll, Chip chip4, Chip chip3, Chip chip2) {
        int checkedId = chipGroupRating.getCheckedChipId();
        if (checkedId == chip4.getId()) {
            return 4f;
        } else if (checkedId == chip3.getId()) {
            return 3f;
        } else if (checkedId == chip2.getId()) {
            return 2f;
        } else if (checkedId == chipAll.getId()) {
            return 0f;
        }
        return filterState.minRating;
    }

    private String formatPriceRange(double min, double max) {
        return String.format(Locale.getDefault(), "R$ %.2f - R$ %.2f", min, max);
    }

    private String getOrNull(MaterialAutoCompleteTextView autoCompleteTextView) {
        CharSequence text = autoCompleteTextView.getText();
        return text != null && text.length() > 0 ? text.toString() : null;
    }

    private void showSortDialog() {
        String[] sortOptions = getResources().getStringArray(R.array.product_sort_options);
        SortOption[] values = SortOption.values();
        int checkedItem = currentSortOption.ordinal();

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.sort_products)
                .setSingleChoiceItems(sortOptions, checkedItem, (dialog, which) -> {
                    currentSortOption = values[which];
                    applyFiltersAndSort();
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onProductClick(Product product) {
        if (getActivity() == null || product == null) {
            return;
        }
        try {
        Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        intent.putExtra("product_name", product.getName());
        intent.putExtra("product_price", product.getPrice());
        intent.putExtra("product_description", product.getDescription());
        intent.putExtra("product_image", product.getImageUrl());
        intent.putExtra("vendor_id", product.getVendorId());
        intent.putExtra("vendor_name", product.getVendorName());
        startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            if (getContext() != null) {
                Toast.makeText(getContext(), "Erro ao abrir produto", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onAddToCart(Product product) {
        if (getContext() == null || product == null || cartManager == null) {
            return;
        }
        try {
            if (!product.isInStock()) {
                Toast.makeText(getContext(), R.string.product_out_of_stock, Toast.LENGTH_SHORT).show();
                return;
            }

            CartItem cartItem = new CartItem(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    1,
                    product.getImageUrl() != null ? product.getImageUrl() : ""
            );
            cartManager.addToCart(cartItem);
            Toast.makeText(getContext(), R.string.message_product_added_to_cart, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            if (getContext() != null) {
                Toast.makeText(getContext(), "Erro ao adicionar ao carrinho", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private enum SortOption {
        BEST_SELLERS,
        PRICE_LOW_HIGH,
        PRICE_HIGH_LOW,
        NEWEST,
        RELEVANCE
    }

    private static class ProductFilterState {
        double minPrice;
        double maxPrice;
        String category;
        String brand;
        String vendor;
        float minRating = 0f;
        boolean inStockQuickToggle = false;
        boolean availabilityInStock = false;
        boolean availabilityOutOfStock = false;
        private boolean priceInitialized = false;

        void ensurePriceInitialized(double min, double max) {
            if (!priceInitialized) {
                this.minPrice = min;
                this.maxPrice = max;
                priceInitialized = true;
            } else {
                this.minPrice = Math.max(min, Math.min(this.minPrice, max));
                this.maxPrice = Math.max(this.minPrice, Math.min(this.maxPrice <= 0 ? max : this.maxPrice, max));
            }
        }

        void reset(double min, double max) {
            this.minPrice = min;
            this.maxPrice = max;
            this.category = null;
            this.brand = null;
            this.vendor = null;
            this.minRating = 0f;
            this.inStockQuickToggle = false;
            this.availabilityInStock = false;
            this.availabilityOutOfStock = false;
        }
    }
}
