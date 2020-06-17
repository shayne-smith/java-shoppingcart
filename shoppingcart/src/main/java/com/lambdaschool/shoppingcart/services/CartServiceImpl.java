package com.lambdaschool.shoppingcart.services;

import com.lambdaschool.shoppingcart.exceptions.ResourceFoundException;
import com.lambdaschool.shoppingcart.exceptions.ResourceNotFoundException;
import com.lambdaschool.shoppingcart.handlers.HelperFunctions;
import com.lambdaschool.shoppingcart.models.Cart;
import com.lambdaschool.shoppingcart.models.CartItem;
import com.lambdaschool.shoppingcart.models.Product;
import com.lambdaschool.shoppingcart.models.User;
import com.lambdaschool.shoppingcart.repositories.CartRepository;
import com.lambdaschool.shoppingcart.repositories.ProductRepository;
import com.lambdaschool.shoppingcart.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service(value = "cartService")
public class CartServiceImpl
        implements CartService
{
    /**
     * Connects this service to the cart repository
     */
    @Autowired
    private CartRepository cartrepos;

    /**
     * Connects this service the user repository
     */
    @Autowired
    private UserRepository userrepos;

    /**
     * Connects this service to the product repository
     */
    @Autowired
    private ProductRepository productrepos;

    /**
     * Connects this service to the auditing service in order to get current user name
     */
    @Autowired
    private UserAuditing userAuditing;

    @Autowired
    private HelperFunctions helper;

    @Autowired
    private ProductService productService;

    @Override
    public List<Cart> findAllByUserId(Long userid)
    {
        return cartrepos.findAllByUser_Userid(userid);
    }

    @Override
    public Cart findCartById(long id)
    {
        return cartrepos.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart id " + id + " not found!"));
    }

    @Transactional
    @Override
    public Cart save(User user,
                     Product product)
    {
        Cart newCart = new Cart();

        User dbuser = userrepos.findById(user.getUserid())
                .orElseThrow(() -> new ResourceNotFoundException("User id " + user.getUserid() + " not found"));
        newCart.setUser(dbuser);

        Product dbproduct = productrepos.findById(product.getProductid())
                .orElseThrow(() -> new ResourceNotFoundException("Product id " + product.getProductid() + " not found"));

        CartItem newCartItem = new CartItem();
        newCartItem.setCart(newCart);
        newCartItem.setProduct(dbproduct);
        newCartItem.setComments("");
        newCartItem.setQuantity(1);
        newCart.getProducts()
                .add(newCartItem);

        return cartrepos.save(newCart);
    }

    @Transactional
    @Override
    public Cart save(Cart cart,
                     Product product)
    {
        Cart updateCart = cartrepos.findById(cart.getCartid())
                .orElseThrow(() -> new ResourceNotFoundException("Cart Id " + cart.getCartid() + " not found"));
        Product updateProduct = productrepos.findById(product.getProductid())
                .orElseThrow(() -> new ResourceNotFoundException("Product id " + product.getProductid() + " not found"));

        if (cartrepos.checkCartItems(updateCart.getCartid(), updateProduct.getProductid())
                .getCount() > 0)
        {
            cartrepos.updateCartItemsQuantity(userAuditing.getCurrentAuditor()
                                                      .get(), updateCart.getCartid(), updateProduct.getProductid(), 1);
        } else
        {
            cartrepos.addCartItems(userAuditing.getCurrentAuditor()
                                           .get(), updateCart.getCartid(), updateProduct.getProductid());
        }

        return cartrepos.save(updateCart);
    }

    @Transactional
    @Override
    public Cart update(
        Cart cart,
        long id)
    {
        Cart currentCart = findCartById(id);

        if (helper.isAuthorizedToMakeChange(currentCart.getUser().getUsername()))
        {
            if (cart.getProducts()
                .size() > 0)
            {
                for (CartItem ci : currentCart.getProducts())
                {
                    delete(ci.getCart(),
                        ci.getProduct());
                }

                for (CartItem ci : cart.getProducts())
                {
                    addCartItem(currentCart.getCartid(),
                        ci.getProduct()
                            .getProductid());
                }
            }

//            if (user.getUseremails()
//                .size() > 0)
//            {
//                currentCart.getUseremails()
//                    .clear();
//                for (Useremail ue : user.getUseremails())
//                {
//                    currentCart.getUseremails()
//                        .add(new Useremail(currentCart,
//                            ue.getUseremail()));
//                }
//            }

            return cartrepos.save(currentCart);
        } else
        {
            {
                throw new ResourceNotFoundException("This user is not authorized to make change");
            }
        }
    }

    @Transactional
    @Override
    public void delete(
        Cart cart,
        Product product)
    {
        Cart currentCart = findCartById(cart.getCartid());

        Cart updateCart = cartrepos.findById(cart.getCartid())
            .orElseThrow(() -> new ResourceNotFoundException("Cart Id " + cart.getCartid() + " not found"));
        Product updateProduct = productrepos.findById(product.getProductid())
            .orElseThrow(() -> new ResourceNotFoundException("Product id " + product.getProductid() + " not found"));

        if (helper.isAuthorizedToMakeChange(currentCart.getUser().getUsername()))
        {
            if (cartrepos.checkCartItems(updateCart.getCartid(), updateProduct.getProductid())
                .getCount() > 0)
            {
                cartrepos.updateCartItemsQuantity(userAuditing.getCurrentAuditor()
                                                          .get(), updateCart.getCartid(), updateProduct.getProductid(), -1);
                cartrepos.removeCartItemsQuantityZero();
                cartrepos.removeCartWithNoProducts();
            }
        } else
        {
            throw new ResourceNotFoundException("Cart id " + updateCart.getCartid() + " Product id " + updateProduct.getProductid() + " combo not found");
        }
    }

//    @Transactional
//    @Override
//    public void delete(Cart cart,
//                       Product product)
//    {
//        Cart updateCart = cartrepos.findById(cart.getCartid())
//                .orElseThrow(() -> new ResourceNotFoundException("Cart Id " + cart.getCartid() + " not found"));
//        Product updateProduct = productrepos.findById(product.getProductid())
//                .orElseThrow(() -> new ResourceNotFoundException("Product id " + product.getProductid() + " not found"));
//
//        if (cartrepos.checkCartItems(updateCart.getCartid(), updateProduct.getProductid())
//                .getCount() > 0)
//        {
//            cartrepos.updateCartItemsQuantity(userAuditing.getCurrentAuditor()
//                                                      .get(), updateCart.getCartid(), updateProduct.getProductid(), -1);
//            cartrepos.removeCartItemsQuantityZero();
//            cartrepos.removeCartWithNoProducts();
//        } else
//        {
//            throw new ResourceNotFoundException("Cart id " + updateCart.getCartid() + " Product id " + updateProduct.getProductid() + " combo not found");
//        }
//    }

    @Transactional
    @Override
    public void addCartItem(
        long cartid,
        long productid)
    {
        cartrepos.findById(cartid)
            .orElseThrow(() -> new ResourceNotFoundException("Cartid " + cartid + " not found!"));
        productService.findProductById(productid);

        if (cartrepos.checkCartItemsCombo(cartid,
            productid)
            .getCount() <= 0)
        {
            cartrepos.insertCartItems(userAuditing.getCurrentAuditor()
                    .get(),
                cartid,
                productid);
        } else
        {
            throw new ResourceFoundException("Cart and Product Combination Already Exists");
        }
    }
}
