package jh.jpaapi.servcie;

import jh.jpaapi.doamin.item.Book;
import jh.jpaapi.doamin.item.Item;
import jh.jpaapi.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item){
        itemRepository.save(item);
    }

    @Transactional
    public Item updateItem(Long itemId, Book param){
        Item findItem = itemRepository.findOne(itemId);
        findItem.setPrice(param.getPrice());
        findItem.setName(param.getName());
        findItem.setStockQuantity(param.getStockQuantity());

        return findItem;
    }

    public List<Item> findItems(){
        return itemRepository.finalAll();
    }

    public Item findOne(Long itemId){
        return itemRepository.findOne(itemId);
    }


}

