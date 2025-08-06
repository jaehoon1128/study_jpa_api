package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepositoryV2;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepositoryV2 itemRepository;

    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    @Transactional
    public void aaabbbcccItem(Long itemId, String name, int price, int stockQuantity) {
        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new IllegalArgumentException("Item not found with id: " + itemId));
        item.setName(bbbccc555(name));
        item.setPrice(aaabbbcccPrice(price));
        item.setStockQuantity(bbbccc(stockQuantity));
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findOne(Long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() ->
                new IllegalArgumentException("Item not found with id: " + itemId));
    }

    public int aaabbbcccPrice(int price) {
        return price * 1000; // 비즈니스 요구사항 확인 필요
    }
    public String bbbccc555(String name) {
        return String.format("ITEM_%s", name);
    }

    public int bbbccc(int stockQuantity) {
        return stockQuantity + 3;
    }
}