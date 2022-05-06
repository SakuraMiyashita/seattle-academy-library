package jp.co.seattle.library.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jp.co.seattle.library.dto.BookDetailsInfo;
import jp.co.seattle.library.service.BooksService;

/**
 * Handles requests for the application home page.
 */
@Controller // APIの入り口
public class BulkBooksController {

	@Autowired
	private BooksService booksService;

	@RequestMapping(value = "/bulkBook", method = RequestMethod.GET) // value＝actionで指定したパラメータ
	// RequestParamでname属性を取得
	public String login(Model model) {
		return "bulkBook";
	}

	/**
	 * 書籍情報を登録する
	 * 
	 * @param locale    ロケール情報
	 * @param title     書籍名
	 * @param author    著者名
	 * @param publisher 出版社
	 * @param file      サムネイルファイル
	 * @param model     モデル_
	 * @return 遷移先画面
	 */
	@Transactional
	@RequestMapping(value = "/bulkRegist", method = RequestMethod.POST, produces = "text/plain;charset=utf-8")
	public String uploadFile(Locale locale, @RequestParam("file") MultipartFile File, Model model) {
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(File.getInputStream(), StandardCharsets.UTF_8))) {
			String line;
			int count = 0;
			List<String[]> booksList = new ArrayList<String[]>();
			List<Integer> errorList = new ArrayList<Integer>();

			if (!br.ready()) {
				model.addAttribute("bulkBookErrorMessage", "CSVに書籍情報がありません。");
				return "bulkBook";
			}

			while ((line = br.readLine()) != null) {
				count = count + 1;
				final String[] split = line.split(",", -1);

				if (StringUtils.isEmpty(split[0]) || StringUtils.isEmpty(split[1]) || StringUtils.isEmpty(split[2])
						|| StringUtils.isEmpty(split[3]) || !(split[3].matches("^[0-9]{8}"))
						|| split[4].length() != 0 && !(split[4].matches("[0-9]{10}|[0-9]{13}"))) {
					errorList.add(count);
				} else {
					booksList.add(split);
				}
			}
			if (errorList.size() > 0) {
				List<String> bulkBookErrorMessage = new ArrayList<String>();
				for (int i = 0; i < errorList.size(); i++) {
					bulkBookErrorMessage.add(errorList.get(i) + "番目にエラーが発生します.");

				}
				model.addAttribute("bulkBookErrorMessage", bulkBookErrorMessage);
				return "bulkBook";
			}

			for (int i = 0; i < booksList.size(); i++) {
				String[] bookList = booksList.get(i);

				BookDetailsInfo bookInfo = new BookDetailsInfo();
				bookInfo.setTitle(bookList[0]);
				bookInfo.setAuthor(bookList[1]);
				bookInfo.setPublisher(bookList[2]);
				bookInfo.setPublishDate(bookList[3]);
				bookInfo.setISBN(bookList[4]);

				booksService.registBook(bookInfo);
				model.addAttribute("resultMessage", "登録完了");

			}
			// final TodoDto todo =
			// TodoDto.builder().id(Integer.parseInt(split[0])).userId(split[1]).action(split[2]).build();
			// todoRepository.insert(todo);
		} catch (IOException e) {
			throw new RuntimeException("ファイルが読み込めません", e);
		}
		return "redirect:home";
	}
}
