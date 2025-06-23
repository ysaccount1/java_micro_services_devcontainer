import React, { useState, useEffect } from 'react';
import ComputerIcon from '@mui/icons-material/Computer';
import PhoneAndroidIcon from '@mui/icons-material/PhoneAndroid';
import HeadsetIcon from '@mui/icons-material/Headset';
import TabletMacIcon from '@mui/icons-material/TabletMac';
import WatchIcon from '@mui/icons-material/Watch';

import { 
    Container, Paper, Typography, List, ListItem, Button, TextField, 
    Grid, Card, CardContent, CardMedia, CardActions, Divider,
    Box, Dialog, DialogTitle, DialogContent, DialogActions,
    Alert, Snackbar, Chip
} from '@mui/material';
import axios from 'axios';

// Use environment variable for API URL
const API_URL = process.env.REACT_APP_SHOPPING_API_URL || "";
// Add a function to get the appropriate icon
const getProductIcon = (iconName) => {
  const iconStyle = { 
    fontSize: 100,
    filter: 'drop-shadow(2px 2px 2px rgba(0,0,0,0.2))'
  };
  
  switch (iconName) {
    case 'Laptop':
      return <ComputerIcon sx={{ ...iconStyle, color: '#2196f3' }} />;
    case 'Smartphone':
      return <PhoneAndroidIcon sx={{ ...iconStyle, color: '#4caf50' }} />;
    case 'Headphones':
      return <HeadsetIcon sx={{ ...iconStyle, color: '#f44336' }} />;
    case 'Tablet':
      return <TabletMacIcon sx={{ ...iconStyle, color: '#ff9800' }} />;
    case 'Smartwatch':
      return <WatchIcon sx={{ ...iconStyle, color: '#9c27b0' }} />;
    default:
      return null;
  }
};
function ShoppingCart() {
    const [cart, setCart] = useState({ items: [], total: 0 });
    const [products, setProducts] = useState([]);
    const [productStocks, setProductStocks] = useState({});
    const [selectedProduct, setSelectedProduct] = useState(null);
    const [quantity, setQuantity] = useState(1);
    const [editItem, setEditItem] = useState(null);
    const [editQuantity, setEditQuantity] = useState(1);
    const [dialogOpen, setDialogOpen] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");
    const [snackbarOpen, setSnackbarOpen] = useState(false);
    const [isResetting, setIsResetting] = useState(false);
    const [sortBy, setSortBy] = useState('price');
    const [sortOrder, setSortOrder] = useState('asc');

    useEffect(() => {
        fetchCart();
        fetchProducts();
    }, []);
    
    // Set default sort to price ascending on initial load
    useEffect(() => {
        setSortBy('price');
        setSortOrder('asc');
    }, []);
    
    // Fetch stock information for all products
    useEffect(() => {
        if (products.length > 0) {
            fetchProductStocks();
        }
    }, [products]);

    const fetchCart = async () => {
        try {
            const userId = localStorage.getItem('userId') || '1';
            const token = localStorage.getItem('token');
            
            const response = await axios.get(`${API_URL}/api/shopping/cart`, {
                headers: { 
                    'userId': userId,
                    'Authorization': `Bearer ${token}`
                }
            });
            setCart(response.data);
        } catch (error) {
            console.error('Error fetching cart:', error);
        }
    };

    const fetchProducts = async () => {
        try {
            const token = localStorage.getItem('token');
            const response = await axios.get(`${API_URL}/api/shopping/products`, {
                headers: { 
                    'Authorization': `Bearer ${token}`
                }
            });
            setProducts(response.data);
        } catch (error) {
            console.error('Error fetching products:', error);
            setErrorMessage("Failed to load products. Please try again later.");
            setSnackbarOpen(true);
        }
    };
    
    const fetchProductStocks = async () => {
        try {
            const stocks = {};
            const token = localStorage.getItem('token');
            // Fetch stock for each product
            for (const product of products) {
                const response = await axios.get(`${API_URL}/api/shopping/products/${product.id}/stock`, {
                    headers: { 
                        'Authorization': `Bearer ${token}`
                    }
                });
                stocks[product.id] = response.data.stock;
            }
            setProductStocks(stocks);
        } catch (error) {
            console.error('Error fetching product stocks:', error);
        }
    };

    const addToCart = async (e) => {
        e.preventDefault();
        if (!selectedProduct) {
            setErrorMessage('Please select a product first');
            setSnackbarOpen(true);
            return;
        }
        
        try {
            const userId = localStorage.getItem('userId') || '1';
            const token = localStorage.getItem('token');
            
            await axios.post(`${API_URL}/api/shopping/cart/add`, {
                productId: selectedProduct.id,
                quantity: parseInt(quantity),
                price: selectedProduct.price
            }, {
                headers: { 
                    'userId': userId,
                    'Authorization': `Bearer ${token}`
                }
            });
            fetchCart();
            fetchProductStocks(); // Refresh stock information
            setQuantity(1);
        } catch (error) {
            console.error('Error adding to cart:', error);
            if (error.response && error.response.data && error.response.data.error) {
                setErrorMessage(error.response.data.error);
                setSnackbarOpen(true);
            } else {
                setErrorMessage('Failed to add item to cart. Please try again.');
                setSnackbarOpen(true);
            }
        }
    };

    const updateCartItem = async () => {
        if (!editItem) return;
        
        try {
            const userId = localStorage.getItem('userId') || '1';
            const token = localStorage.getItem('token');
            
            await axios.put(`${API_URL}/api/shopping/cart/update/${editItem.id}?quantity=${editQuantity}`, {}, {
                headers: { 
                    'userId': userId,
                    'Authorization': `Bearer ${token}`
                }
            });
            fetchCart();
            fetchProductStocks(); // Refresh stock information
            setDialogOpen(false);
        } catch (error) {
            console.error('Error updating cart item:', error);
            if (error.response && error.response.data && error.response.data.error) {
                setErrorMessage(error.response.data.error);
                setSnackbarOpen(true);
            } else {
                setErrorMessage('Failed to update cart item. Please try again.');
                setSnackbarOpen(true);
            }
        }
    };

    const removeCartItem = async (itemId) => {
        try {
            const userId = localStorage.getItem('userId') || '1';
            const token = localStorage.getItem('token');
            
            await axios.delete(`${API_URL}/api/shopping/cart/remove/${itemId}`, {
                headers: { 
                    'userId': userId,
                    'Authorization': `Bearer ${token}`
                }
            });
            fetchCart();
            fetchProductStocks(); // Refresh stock information
        } catch (error) {
            console.error('Error removing cart item:', error);
            setErrorMessage('Failed to remove item from cart. Please try again.');
            setSnackbarOpen(true);
        }
    };
    
    const resetEnvironment = async () => {
        try {
            setIsResetting(true);
            const token = localStorage.getItem('token');
            await axios.post(`${API_URL}/api/admin/reset`, {}, {
                headers: { 
                    'Authorization': `Bearer ${token}`
                }
            });
            fetchProducts();
            fetchCart();
            setErrorMessage('Environment reset successfully');
            setSnackbarOpen(true);
        } catch (error) {
            console.error('Error resetting environment:', error);
            setErrorMessage('Failed to reset environment');
            setSnackbarOpen(true);
        } finally {
            setIsResetting(false);
        }
    };

    const handleSnackbarClose = () => {
        setSnackbarOpen(false);
    };

    const handleEditClick = (item) => {
        setEditItem(item);
        setEditQuantity(item.quantity);
        setDialogOpen(true);
    };

    const selectProduct = (product) => {
        setSelectedProduct(product);
    };

    const sortProducts = (products) => {
        return [...products].sort((a, b) => {
            const factor = sortOrder === 'asc' ? 1 : -1;
            
            switch (sortBy) {
                case 'price':
                    return (a.price - b.price) * factor;
                case 'name':
                    return a.name.localeCompare(b.name) * factor;
                case 'stock':
                    const stockA = productStocks[a.id] || 0;
                    const stockB = productStocks[b.id] || 0;
                    return (stockA - stockB) * factor;
                default:
                    return 0;
            }
        });
    };

    const getProductName = (productId) => {
        const product = products.find(p => p.id === productId);
        return product ? product.name : `Product ${productId}`;
    };

    return (
        <Container maxWidth="lg">
            <Paper elevation={3} style={{ padding: '20px', marginTop: '20px' }}>
                <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                    <Typography variant="h4">Shopping Cart</Typography>
                    <Box>
                        <Button 
                            variant="contained" 
                            color="error" 
                            onClick={resetEnvironment}
                            disabled={isResetting}
                            sx={{ mr: 2 }}
                        >
                            {isResetting ? "Resetting..." : "Reset Environment"}
                        </Button>
                    </Box>
                </Box>

                <Grid container spacing={3}>
                    {/* Product List */}
                    <Grid item xs={12} md={8}>
                        <Typography variant="h5" gutterBottom>Available Products</Typography>
                        <Box display="flex" alignItems="center" mb={2}>
                            <Typography variant="subtitle1" mr={2}>Sort by:</Typography>
                            <Button 
                                variant={sortBy === 'price' ? 'contained' : 'outlined'}
                                size="small"
                                onClick={() => {
                                    setSortBy('price');
                                    setSortOrder(sortBy === 'price' && sortOrder === 'asc' ? 'desc' : 'asc');
                                }}
                                sx={{ mr: 1 }}
                            >
                                Price {sortBy === 'price' && (sortOrder === 'asc' ? '↑' : '↓')}
                            </Button>
                            <Button 
                                variant={sortBy === 'name' ? 'contained' : 'outlined'}
                                size="small"
                                onClick={() => {
                                    setSortBy('name');
                                    setSortOrder(sortBy === 'name' && sortOrder === 'asc' ? 'desc' : 'asc');
                                }}
                                sx={{ mr: 1 }}
                            >
                                Name {sortBy === 'name' && (sortOrder === 'asc' ? '↑' : '↓')}
                            </Button>
                            <Button 
                                variant={sortBy === 'stock' ? 'contained' : 'outlined'}
                                size="small"
                                onClick={() => {
                                    setSortBy('stock');
                                    setSortOrder(sortBy === 'stock' && sortOrder === 'asc' ? 'desc' : 'asc');
                                }}
                            >
                                Stock {sortBy === 'stock' && (sortOrder === 'asc' ? '↑' : '↓')}
                            </Button>
                        </Box>
                        <Grid container spacing={2}>
                            {sortProducts(products).map((product) => (
                                <Grid item xs={12} sm={6} md={4} key={product.id}>
                                    <Card 
                                        sx={{ 
                                            height: '100%', 
                                            display: 'flex', 
                                            flexDirection: 'column',
                                            border: selectedProduct && selectedProduct.id === product.id ? '2px solid #1976d2' : 'none'
                                        }}
                                        onClick={() => selectProduct(product)}
                                    >
                                        <Box 
                                            display="flex" 
                                            justifyContent="center" 
                                            alignItems="center" 
                                            height={140} 
                                            sx={{
                                                background: 'linear-gradient(45deg, #f5f5f5 30%, #e0e0e0 90%)',
                                                borderRadius: '4px 4px 0 0'
                                            }}
                                        >
                                            {getProductIcon(product.name)}
                                        </Box>
                                        <CardContent sx={{ flexGrow: 1 }}>
                                            <Typography gutterBottom variant="h6" component="div">
                                                {product.name}
                                            </Typography>
                                            <Typography variant="body2" color="text.secondary">
                                                {product.description}
                                            </Typography>
                                            <Box display="flex" justifyContent="space-between" alignItems="center" mt={2}>
                                                <Typography variant="h6" color="primary">
                                                    ${product.price}
                                                </Typography>
                                                {productStocks[product.id] !== undefined && (
                                                    <Chip 
                                                        label={productStocks[product.id] > 0 ? `In Stock: ${productStocks[product.id]}` : "Out of Stock"} 
                                                        color={productStocks[product.id] > 0 ? "success" : "error"}
                                                        size="small"
                                                    />
                                                )}
                                            </Box>
                                        </CardContent>
                                        <CardActions>
                                            <Button 
                                                size="small" 
                                                variant="contained" 
                                                fullWidth
                                                onClick={() => selectProduct(product)}
                                                disabled={productStocks[product.id] === 0}
                                            >
                                                {productStocks[product.id] === 0 ? "Out of Stock" : "Select"}
                                            </Button>
                                        </CardActions>
                                    </Card>
                                </Grid>
                            ))}
                        </Grid>
                    </Grid>

                    {/* Cart Section */}
                    <Grid item xs={12} md={4}>
                        <Paper elevation={2} sx={{ p: 2 }}>
                            <Typography variant="h5" gutterBottom>Your Cart</Typography>
                            
                            {selectedProduct && (
                                <div>
                                    <Typography variant="h6">Selected Product:</Typography>
                                    <Typography>{selectedProduct.name} - ${selectedProduct.price}</Typography>
                                    
                                    <form onSubmit={addToCart} style={{ marginTop: '10px' }}>
                                        <TextField
                                            fullWidth
                                            label="Quantity"
                                            type="number"
                                            margin="normal"
                                            value={quantity}
                                            onChange={(e) => setQuantity(e.target.value)}
                                            inputProps={{ 
                                                min: 1, 
                                                max: productStocks[selectedProduct.id] || 1 
                                            }}
                                            helperText={
                                                productStocks[selectedProduct.id] !== undefined 
                                                ? `Available: ${productStocks[selectedProduct.id]}` 
                                                : ""
                                            }
                                        />
                                        <Button
                                            fullWidth
                                            variant="contained"
                                            color="primary"
                                            type="submit"
                                            style={{ marginTop: '10px' }}
                                            disabled={productStocks[selectedProduct.id] === 0}
                                        >
                                            {productStocks[selectedProduct.id] === 0 ? "Out of Stock" : "Add to Cart"}
                                        </Button>
                                    </form>
                                </div>
                            )}
                            
                            <Divider sx={{ my: 2 }} />
                            
                            <Typography variant="h6">Cart Items:</Typography>
                            {cart.items.length === 0 ? (
                                <Typography>Your cart is empty</Typography>
                            ) : (
                                <List>
                                    {cart.items.map((item) => (
                                        <ListItem 
                                            key={item.id} 
                                            divider
                                        >
                                            <Box sx={{ width: '100%', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                                <Box>
                                                    <Typography variant="subtitle1">
                                                        {getProductName(item.productId)}
                                                    </Typography>
                                                    <Typography variant="body2">
                                                        Quantity: {item.quantity} × ${item.price} = ${(item.quantity * item.price).toFixed(2)}
                                                    </Typography>
                                                </Box>
                                                <Box>
                                                    <Button 
                                                        size="small"
                                                        onClick={() => handleEditClick(item)}
                                                    >
                                                        Edit
                                                    </Button>
                                                    <Button 
                                                        size="small"
                                                        color="error"
                                                        onClick={() => removeCartItem(item.id)}
                                                    >
                                                        Remove
                                                    </Button>
                                                </Box>
                                            </Box>
                                        </ListItem>
                                    ))}
                                    <Box sx={{ mt: 2, p: 2, bgcolor: '#f5f5f5', borderRadius: 1 }}>
                                        <Typography variant="h6" align="right">
                                            Total Amount: ${cart.total.toFixed(2)}
                                        </Typography>
                                    </Box>
                                </List>
                            )}
                        </Paper>
                    </Grid>
                </Grid>
            </Paper>

            {/* Edit Item Dialog */}
            <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)}>
                <DialogTitle>Edit Cart Item</DialogTitle>
                <DialogContent>
                    {editItem && (
                        <>
                            <Typography variant="subtitle1">
                                {getProductName(editItem.productId)}
                            </Typography>
                            <Typography variant="body2" gutterBottom>
                                Price: ${editItem.price}
                            </Typography>
                            <TextField
                                autoFocus
                                margin="dense"
                                label="Quantity"
                                type="number"
                                fullWidth
                                value={editQuantity}
                                onChange={(e) => setEditQuantity(parseInt(e.target.value))}
                                inputProps={{ 
                                    min: 1,
                                    max: editItem && productStocks[editItem.productId] 
                                        ? productStocks[editItem.productId] + editItem.quantity 
                                        : 999
                                }}
                                helperText={
                                    editItem && productStocks[editItem.productId] !== undefined
                                    ? `Available: ${productStocks[editItem.productId] + editItem.quantity}`
                                    : ""
                                }
                            />
                        </>
                    )}
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
                    <Button onClick={updateCartItem} color="primary">Save</Button>
                </DialogActions>
            </Dialog>
            
            {/* Error Snackbar */}
            <Snackbar 
                open={snackbarOpen} 
                autoHideDuration={6000} 
                onClose={handleSnackbarClose}
                anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
            >
                <Alert onClose={handleSnackbarClose} severity="error" sx={{ width: '100%' }}>
                    {errorMessage}
                </Alert>
            </Snackbar>
        </Container>
    );
}

export default ShoppingCart;