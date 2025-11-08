# Thi Yên Store

E-commerce application built with Vue.js 3, Vite, and Tailwind CSS.

## Environment Configuration

The application uses environment variables to configure API endpoints for different environments.

### Environment Files

- `env.local` - Local development environment
- `env.production` - Production environment

### Environment Variables

| Variable | Description | Local | Production |
|----------|-------------|-------|------------|
| `VITE_API_BASE_URL` | API base URL | `http://localhost:8080` | `https://www.phodem.click` |
| `VITE_APP_TITLE` | Application title | `Thi Yên Store` | `Thi Yên Store` |
| `VITE_APP_DESCRIPTION` | Application description | `Your trusted source for quality products` | `Your trusted source for quality products` |

## Development

```bash
# Install dependencies
npm install

# Start development server
npm run dev
```

## Building for Different Environments

```bash
# Build for local environment
npm run build:local

# Build for production environment
npm run build:prod

# Build for current environment (default)
npm run build
```

## API Configuration

The application uses a centralized API utility (`src/utils/api.js`) that automatically uses the correct base URL based on the environment.

### Available API Functions

#### Product API
- `productAPI.getProducts()` - Get all products
- `productAPI.getProduct(id)` - Get product by ID
- `productAPI.getProductDetails(id)` - Get product details

#### Order API
- `orderAPI.createOrder(orderData)` - Create new order
- `orderAPI.getOrders()` - Get all orders
- `orderAPI.updateOrderStatus(orderId, status)` - Update order status

#### Admin API
- `adminAPI.login(credentials)` - Admin login
- `adminAPI.getAdminProducts()` - Get admin products
- `adminAPI.saveProduct(productData)` - Create/Update product
- `adminAPI.deleteProduct(productId)` - Delete product

## Project Structure

```
src/
├── components/          # Vue components
├── views/              # Page components
├── stores/             # Pinia stores
├── router/             # Vue Router configuration
├── utils/              # Utility functions
│   └── api.js         # API utility functions
└── assets/             # Static assets
```

## Features

- 🛍️ Product catalog with detailed views
- 🛒 Shopping cart functionality
- 💳 Checkout process
- 👤 Admin panel with product and order management
- 📱 Responsive design
- ⚡ Fast development with Vite
- 🎨 Modern UI with Tailwind CSS

## Technologies Used

- Vue.js 3 (Composition API)
- Vite
- Vue Router 4
- Pinia (State Management)
- Tailwind CSS
- Headless UI
- FontAwesome Icons

## License

This project is private and proprietary.
