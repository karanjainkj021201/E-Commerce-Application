export interface Page<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface Category {
  id: number;
  name: string;
  code: string;
  description: string;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface Product {
  id: number;
  sku: string;
  name: string;
  description: string;
  price: number;
  currency: string;
  imageUrl?: string;
  status: 'ACTIVE' | 'INACTIVE' | 'ARCHIVED';
  category: Category;
  createdAt?: string;
  updatedAt?: string;
}

export interface InventoryAvailability {
  productId: number;
  sku: string;
  warehouseCode: string;
  availableQuantity: number;
  available: boolean;
}

export interface Stock {
  id: number;
  productId: number;
  sku: string;
  productName: string;
  warehouseCode: string;
  totalQuantity: number;
  reservedQuantity: number;
  availableQuantity: number;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface UserProfile {
  id: number;
  name: string;
  email: string;
  phone: string;
  status: 'ACTIVE' | 'INACTIVE' | 'BLOCKED';
  createdAt?: string;
  updatedAt?: string;
}

export interface OrderItem {
  id: number;
  productId: number;
  sku: string;
  productNameSnapshot: string;
  unitPriceSnapshot: number;
  currency: string;
  quantity: number;
  lineTotal: number;
}

export interface Order {
  id: number;
  orderNumber: string;
  keycloakUserId: string;
  status: string;
  paymentStatus: string;
  inventoryStatus: string;
  shippingStatus: string;
  subtotal: number;
  shippingFee: number;
  totalAmount: number;
  currency: string;
  paymentMethod: string;
  paymentReference?: string;
  shipmentId?: string;
  carrier?: string;
  trackingNumber?: string;
  failureReason?: string;
  customerName: string;
  customerEmail: string;
  customerPhone: string;
  shippingLine1: string;
  shippingLine2?: string;
  shippingCity: string;
  shippingState: string;
  shippingPincode: string;
  shippingCountry: string;
  items: OrderItem[];
  createdAt: string;
  updatedAt: string;
}

export interface Refund {
  id: number;
  refundReference: string;
  amount: number;
  currency: string;
  status: string;
  reason?: string;
  createdAt: string;
  completedAt?: string;
}

export interface Payment {
  id: number;
  orderId: number;
  orderNumber: string;
  keycloakUserId: string;
  amount: number;
  currency: string;
  provider: string;
  status: string;
  paymentMethod: string;
  paymentReference: string;
  gatewayReference?: string;
  gatewayPaymentUrl?: string;
  failureReason?: string;
  refunds: Refund[];
  createdAt: string;
  updatedAt: string;
}

export interface ShipmentHistory {
  id: number;
  status: string;
  note?: string;
  occurredAt: string;
}

export interface Shipment {
  id: number;
  shipmentNumber: string;
  orderId: number;
  orderNumber: string;
  keycloakUserId: string;
  status: string;
  carrier?: string;
  trackingNumber?: string;
  cancellationReason?: string;
  statusHistory: ShipmentHistory[];
  createdAt: string;
  updatedAt: string;
  deliveredAt?: string;
}

export interface Tracking {
  shipmentNumber: string;
  orderNumber: string;
  status: string;
  carrier?: string;
  trackingNumber: string;
  statusHistory: ShipmentHistory[];
  createdAt: string;
  deliveredAt?: string;
}

export interface CartItem {
  product: Product;
  quantity: number;
}

export interface CreateOrderRequest {
  items: Array<{ productId: number; quantity: number }>;
  shippingAddress: {
    customerName: string;
    customerEmail: string;
    customerPhone: string;
    line1: string;
    line2?: string;
    city: string;
    state: string;
    pincode: string;
    country: string;
  };
  paymentMethod: string;
  shippingFee: number;
}
